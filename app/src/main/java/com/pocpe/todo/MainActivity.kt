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
import com.pocpe.todo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var callbackManager: CallbackManager
    private lateinit var mixpanel: MixpanelAPI
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        
        // Initialize Mixpanel
        val mixpanelToken = getString(R.string.mixpanel_token)
        mixpanel = MixpanelAPI.getInstance(
            this,
            if (mixpanelToken != "YOUR_MIXPANEL_TOKEN") mixpanelToken else "dummy_token"
        )
        
        // Initialize Facebook Callback Manager
        callbackManager = CallbackManager.Factory.create()
        
        // Check if user is already logged in
        val currentUser = auth.currentUser
        if (currentUser != null) {
            navigateToHome(currentUser)
            return
        }
        
        setupEmailLogin()
        setupFacebookLogin()
    }
    
    private fun setupEmailLogin() {
        binding.btnLoginEmail.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            binding.progressBar.visibility = android.view.View.VISIBLE
            
            // Track login attempt in Mixpanel
            mixpanel.track("Login Attempt", mapOf("method" to "email"))
            
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    binding.progressBar.visibility = android.view.View.GONE
                    
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        if (user != null) {
                            // Track successful login
                            mixpanel.track("Login Success", mapOf("method" to "email"))
                            mixpanel.identify(user.uid)
                            mixpanel.getPeople().set("email", user.email)
                            
                            navigateToHome(user)
                        }
                    } else {
                        // Track failed login
                        mixpanel.track("Login Failed", mapOf(
                            "method" to "email",
                            "error" to (task.exception?.message ?: "Unknown error")
                        ))
                        Toast.makeText(
                            this,
                            "Authentication failed: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }
    
    private fun setupFacebookLogin() {
        binding.btnLoginFacebook.setPermissions("email", "public_profile")
        binding.btnLoginFacebook.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                handleFacebookAccessToken(loginResult.accessToken.token)
            }
            
            override fun onCancel() {
                Toast.makeText(this@MainActivity, "Facebook login cancelled", Toast.LENGTH_SHORT).show()
                mixpanel.track("Login Cancelled", mapOf("method" to "facebook"))
            }
            
            override fun onError(error: FacebookException) {
                Toast.makeText(this@MainActivity, "Facebook login error: ${error.message}", Toast.LENGTH_SHORT).show()
                mixpanel.track("Login Error", mapOf(
                    "method" to "facebook",
                    "error" to (error.message ?: "Unknown error")
                ))
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
                        mixpanel.track("Login Success", mapOf("method" to "facebook"))
                        mixpanel.identify(user.uid)
                        mixpanel.getPeople().set("email", user.email)
                        
                        navigateToHome(user)
                    }
                } else {
                    mixpanel.track("Login Failed", mapOf(
                        "method" to "facebook",
                        "error" to (task.exception?.message ?: "Unknown error")
                    ))
                    Toast.makeText(
                        this,
                        "Authentication failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
    
    private fun navigateToHome(user: FirebaseUser) {
        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra("user_email", user.email)
        startActivity(intent)
        finish()
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
