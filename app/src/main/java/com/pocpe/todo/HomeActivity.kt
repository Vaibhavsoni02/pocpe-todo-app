package com.pocpe.todo

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.pocpe.todo.databinding.ActivityHomeBinding
import org.json.JSONObject

class HomeActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityHomeBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var mixpanel: MixpanelAPI
    private var bluetoothAdapter: BluetoothAdapter? = null
    private lateinit var connectivityManager: ConnectivityManager
    
    private val PERMISSIONS_REQUEST_CODE = 100
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        auth = FirebaseAuth.getInstance()
        val mixpanelToken = getString(R.string.mixpanel_token)
        mixpanel = MixpanelAPI.getInstance(
            this,
            if (mixpanelToken != "YOUR_MIXPANEL_TOKEN") mixpanelToken else "dummy_token",
            true  // opt-out tracking enabled
        )
        
        // Initialize Bluetooth
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        bluetoothAdapter = bluetoothManager?.adapter
        
        // Initialize Connectivity Manager
        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        // Get user email from intent
        val userEmail = intent.getStringExtra("user_email") ?: auth.currentUser?.email ?: "User"
        binding.tvUserEmail.text = "Logged in as: $userEmail"
        
        // Identify user in Mixpanel
        auth.currentUser?.let { user ->
            mixpanel.identify(user.uid)
            mixpanel.getPeople().set("email", user.email)
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
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Toast.makeText(this, "Bluetooth permissions granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Bluetooth permissions denied", Toast.LENGTH_SHORT).show()
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
    
    override fun onDestroy() {
        super.onDestroy()
        mixpanel.flush()
    }
}
