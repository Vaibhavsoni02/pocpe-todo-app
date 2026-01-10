package com.pocpe.todo

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.pocpe.todo.databinding.ActivityHomeBinding
import org.json.JSONObject
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.text.DecimalFormat

class HomeActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityHomeBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var mixpanel: MixpanelAPI
    private var bluetoothAdapter: BluetoothAdapter? = null
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var cameraManager: CameraManager
    private var isFlashlightOn = false
    private var cameraId: String? = null
    
    private val PERMISSIONS_REQUEST_CODE = 100
    private val CAMERA_PERMISSION_REQUEST_CODE = 101
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        auth = FirebaseAuth.getInstance()
        val mixpanelToken = getString(R.string.mixpanel_token)
        mixpanel = MixpanelAPI.getInstance(
            this,
            mixpanelToken,
            true  // opt-out tracking enabled
        )
        
        // Initialize Bluetooth
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        bluetoothAdapter = bluetoothManager?.adapter
        
        // Initialize Connectivity Manager
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        // Initialize Camera Manager
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            cameraId = cameraManager.cameraIdList[0]
        } catch (e: Exception) {
            android.util.Log.w("HomeActivity", "Flashlight not available: ${e.message}")
        }
        
        // Get user email from intent
        val userEmail = intent.getStringExtra("user_email") ?: auth.currentUser?.email ?: "User"
        binding.tvUserEmail.text = "Logged in as: $userEmail"
        
        // Identify user in Mixpanel with email
        auth.currentUser?.let { user ->
            user.email?.let { email ->
                mixpanel.identify(email)
                updateMixpanelUserProfile(user, email)
            }
        }
        
        // Track screen view
        mixpanel.track("Home Screen Viewed")
        
        setupButtons()
        checkPermissions()
    }
    
    private fun setupButtons() {
        // Check Internet Button
        binding.btnCheckInternet.setOnClickListener {
            checkInternetConnection()
            mixpanel.track("Internet Check Button Clicked")
        }
        
        // Check Bluetooth Button
        binding.btnCheckBluetooth.setOnClickListener {
            checkBluetoothStatus()
            mixpanel.track("Bluetooth Check Button Clicked")
        }
        
        // Internet Speed Button
        binding.btnCheckInternetSpeed.setOnClickListener {
            checkInternetSpeed()
            mixpanel.track("Internet Speed Check Button Clicked")
        }
        
        // Toggle Flashlight Button
        binding.btnToggleFlashlight.setOnClickListener {
            toggleFlashlight()
            mixpanel.track("Flashlight Toggle Button Clicked")
        }
        
        // User Profile Button
        binding.btnUserProfile.setOnClickListener {
            val intent = Intent(this, UserProfileActivity::class.java)
            startActivity(intent)
            mixpanel.track("User Profile Button Clicked")
        }
        
        // Logout Button
        binding.btnLogout.setOnClickListener {
            logout()
            mixpanel.track("Logout Button Clicked")
        }
    }
    
    private fun checkInternetConnection() {
        val isConnected = isInternetAvailable()
        val status = if (isConnected) {
            getString(R.string.internet_connected)
        } else {
            getString(R.string.internet_disconnected)
        }
        
        binding.tvInternetStatus.text = status
        Toast.makeText(this, status, Toast.LENGTH_SHORT).show()
        
        val internetProps = JSONObject().apply {
            put("connected", isConnected)
        }
        mixpanel.track("Internet Status Checked", internetProps)
    }
    
    private fun isInternetAvailable(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                   capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }
    }
    
    private fun checkBluetoothStatus() {
        if (bluetoothAdapter == null) {
            binding.tvBluetoothStatus.text = "Bluetooth not available on this device"
            Toast.makeText(this, "Bluetooth not available", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (!hasBluetoothPermissions()) {
            requestBluetoothPermissions()
            return
        }
        
        val isEnabled = bluetoothAdapter?.isEnabled == true
        val status = if (isEnabled) {
            getString(R.string.bluetooth_enabled)
        } else {
            getString(R.string.bluetooth_disabled)
        }
        
        binding.tvBluetoothStatus.text = status
        Toast.makeText(this, status, Toast.LENGTH_SHORT).show()
        
        val bluetoothProps = JSONObject().apply {
            put("enabled", isEnabled)
        }
        mixpanel.track("Bluetooth Status Checked", bluetoothProps)
    }
    
    private fun hasBluetoothPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == 
                PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == 
                PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == 
                PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) == 
                PackageManager.PERMISSION_GRANTED
        }
    }
    
    private fun requestBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN
                ),
                PERMISSIONS_REQUEST_CODE
            )
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN
                ),
                PERMISSIONS_REQUEST_CODE
            )
        }
    }
    
    private fun checkPermissions() {
        if (!hasBluetoothPermissions()) {
            requestBluetoothPermissions()
        }
    }
    
    private fun checkInternetSpeed() {
        if (!isInternetAvailable()) {
            binding.tvInternetSpeed.text = "No internet connection"
            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
            return
        }
        
        binding.tvInternetSpeed.text = getString(R.string.speed_testing)
        binding.btnCheckInternetSpeed.isEnabled = false
        
        // Simple speed test using a small file download
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val testUrl = "https://www.google.com/images/branding/googlelogo/1x/googlelogo_color_272x92dp.png"
                val url = URL(testUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                
                val startTime = System.currentTimeMillis()
                var totalBytes = 0L
                connection.inputStream.use { input ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        totalBytes += bytesRead
                        // Stop after 1MB to keep it quick
                        if (totalBytes > 1024 * 1024) break
                    }
                }
                val endTime = System.currentTimeMillis()
                val duration = (endTime - startTime).toDouble() / 1000.0 // seconds
                val speedMbps = (totalBytes * 8.0) / (duration * 1024.0 * 1024.0) // Mbps
                
                val df = DecimalFormat("#.##")
                
                withContext(Dispatchers.Main) {
                    binding.tvInternetSpeed.text = getString(R.string.download_speed, "${df.format(speedMbps)} Mbps")
                    binding.btnCheckInternetSpeed.isEnabled = true
                    
                    val speedProps = JSONObject().apply {
                        put("speed_mbps", speedMbps.toDouble())
                        put("duration_seconds", duration)
                    }
                    mixpanel.track("Internet Speed Tested", speedProps)
                }
                
                connection.disconnect()
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.tvInternetSpeed.text = getString(R.string.speed_error)
                    binding.btnCheckInternetSpeed.isEnabled = true
                    Toast.makeText(this@HomeActivity, "Speed test failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun toggleFlashlight() {
        if (!hasCameraPermission()) {
            requestCameraPermission()
            return
        }
        
        if (cameraId == null) {
            Toast.makeText(this, "Flashlight not available on this device", Toast.LENGTH_SHORT).show()
            return
        }
        
        try {
            if (isFlashlightOn) {
                cameraManager.setTorchMode(cameraId!!, false)
                isFlashlightOn = false
                binding.tvFlashlightStatus.text = getString(R.string.flashlight_off)
            } else {
                cameraManager.setTorchMode(cameraId!!, true)
                isFlashlightOn = true
                binding.tvFlashlightStatus.text = getString(R.string.flashlight_on)
            }
            
            val flashlightProps = JSONObject().apply {
                put("flashlight_on", isFlashlightOn)
            }
            mixpanel.track("Flashlight Toggled", flashlightProps)
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to toggle flashlight: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST_CODE
        )
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    Toast.makeText(this, "Bluetooth permissions granted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Bluetooth permissions denied", Toast.LENGTH_SHORT).show()
                }
            }
            CAMERA_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, getString(R.string.camera_permission_required), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    override fun onStop() {
        super.onStop()
        // Turn off flashlight when leaving the activity
        if (isFlashlightOn && cameraId != null) {
            try {
                cameraManager.setTorchMode(cameraId!!, false)
                isFlashlightOn = false
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
    
    private fun logout() {
        auth.signOut()
        
        // Track logout event
        mixpanel.track("User Logged Out")
        mixpanel.reset()
        mixpanel.flush()
        
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
        
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
    }
    
    /**
     * Update Mixpanel user profile with user information and Android Advertising ID
     */
    private fun updateMixpanelUserProfile(user: FirebaseUser, email: String) {
        // Set user properties
        mixpanel.getPeople().set("email", email)
        mixpanel.getPeople().set("user_id", user.uid)
        user.displayName?.let { displayName ->
            mixpanel.getPeople().set("name", displayName)
        }
        
        // Get and set Android Advertising ID (GAID) asynchronously
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val adInfo = AdvertisingIdClient.getAdvertisingIdInfo(applicationContext)
                val advertisingId = adInfo?.id
                val isLimitAdTrackingEnabled = adInfo?.isLimitAdTrackingEnabled ?: false
                
                withContext(Dispatchers.Main) {
                    advertisingId?.let { gaid ->
                        mixpanel.getPeople().set("android_ad_id", gaid)
                        mixpanel.getPeople().set("gps_adid", gaid) // Alternative name
                        mixpanel.getPeople().set("limit_ad_tracking", isLimitAdTrackingEnabled)
                    }
                }
            } catch (e: Exception) {
                // Advertising ID might not be available (e.g., on emulator or restricted)
                // Log but don't fail the process
                android.util.Log.w("HomeActivity", "Failed to get Advertising ID: ${e.message}")
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        mixpanel.flush()
    }
}
