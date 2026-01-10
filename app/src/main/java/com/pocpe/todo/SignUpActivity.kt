package com.pocpe.todo

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.pocpe.todo.databinding.ActivitySignUpBinding
import org.json.JSONObject
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SignUpActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var callbackManager: CallbackManager
    private lateinit var mixpanel: MixpanelAPI
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        
        // Initialize Mixpanel
        val mixpanelToken = getString(R.string.mixpanel_token)
        mixpanel = MixpanelAPI.getInstance(
            this,
            mixpanelToken,
            true  // opt-out tracking enabled
        )
        
        // Initialize Facebook Callback Manager
        callbackManager = CallbackManager.Factory.create()
        
        setupSignUp()
        setupFacebookSignUp()
        setupLoginNavigation()
    }
    
    private fun setupSignUp() {
        binding.btnSignUp.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()
            
            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            binding.progressBar.visibility = android.view.View.VISIBLE
            
            // Track sign up attempt in Mixpanel
            val properties = JSONObject().apply {
                put("method", "email")
            }
            mixpanel.track("Sign Up Attempt", properties)
            
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    binding.progressBar.visibility = android.view.View.GONE
                    
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        if (user != null) {
                            // Track successful sign up
                            val successProps = JSONObject().apply {
                                put("method", "email")
                            }
                            mixpanel.track("Sign Up Success", successProps)
                            
                            // Identify user with email
                            user.email?.let { email ->
                                mixpanel.identify(email)
                                updateMixpanelUserProfile(user, email)
                            }
                            
                            Toast.makeText(this, getString(R.string.account_created), Toast.LENGTH_SHORT).show()
                            navigateToHome(user)
                        }
                    } else {
                        // Track failed sign up
                        val failedProps = JSONObject().apply {
                            put("method", "email")
                            put("error", task.exception?.message ?: "Unknown error")
                        }
                        mixpanel.track("Sign Up Failed", failedProps)
                        Toast.makeText(
                            this,
                            getString(R.string.sign_up_failed, task.exception?.message ?: "Unknown error"),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }
    
    private fun setupFacebookSignUp() {
        binding.btnSignUpFacebook.setPermissions("email", "public_profile")
        binding.btnSignUpFacebook.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                handleFacebookAccessToken(loginResult.accessToken.token)
            }
            
            override fun onCancel() {
                Toast.makeText(this@SignUpActivity, "Facebook sign up cancelled", Toast.LENGTH_SHORT).show()
                val cancelledProps = JSONObject().apply {
                    put("method", "facebook")
                }
                mixpanel.track("Sign Up Cancelled", cancelledProps)
            }
            
            override fun onError(error: FacebookException) {
                Toast.makeText(this@SignUpActivity, "Facebook sign up error: ${error.message}", Toast.LENGTH_SHORT).show()
                val errorProps = JSONObject().apply {
                    put("method", "facebook")
                    put("error", error.message ?: "Unknown error")
                }
                mixpanel.track("Sign Up Error", errorProps)
            }
        })
    }
    
    private fun handleFacebookAccessToken(token: String) {
        binding.progressBar.visibility = android.view.View.VISIBLE
        
        val credential = FacebookAuthProvider.getCredential(token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                binding.progressBar.visibility = android.view.View.GONE
                
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        val fbSuccessProps = JSONObject().apply {
                            put("method", "facebook")
                        }
                        mixpanel.track("Sign Up Success", fbSuccessProps)
                        
                        // Identify user with email
                        user.email?.let { email ->
                            mixpanel.identify(email)
                            updateMixpanelUserProfile(user, email)
                        }
                        
                        navigateToHome(user)
                    }
                } else {
                    val fbFailedProps = JSONObject().apply {
                        put("method", "facebook")
                        put("error", task.exception?.message ?: "Unknown error")
                    }
                    mixpanel.track("Sign Up Failed", fbFailedProps)
                    Toast.makeText(
                        this,
                        "Authentication failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
    
    private fun setupLoginNavigation() {
        binding.tvLogin.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    
    private fun navigateToHome(user: FirebaseUser) {
        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra("user_email", user.email)
        startActivity(intent)
        finish()
    }
    
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
                        mixpanel.getPeople().set("gps_adid", gaid)
                        mixpanel.getPeople().set("limit_ad_tracking", isLimitAdTrackingEnabled)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.w("SignUpActivity", "Failed to get Advertising ID: ${e.message}")
            }
        }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        mixpanel.flush()
    }
}
