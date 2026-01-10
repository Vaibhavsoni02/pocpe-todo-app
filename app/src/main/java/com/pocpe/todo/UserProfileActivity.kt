package com.pocpe.todo

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.pocpe.todo.databinding.ActivityUserProfileBinding
import java.text.SimpleDateFormat
import java.util.*

class UserProfileActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityUserProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var mixpanel: MixpanelAPI
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        auth = FirebaseAuth.getInstance()
        val mixpanelToken = getString(R.string.mixpanel_token)
        mixpanel = MixpanelAPI.getInstance(
            this,
            mixpanelToken,
            true
        )
        
        // Track screen view
        mixpanel.track("User Profile Screen Viewed")
        
        setupProfile()
        setupBackButton()
    }
    
    private fun setupProfile() {
        val user = auth.currentUser
        
        if (user != null) {
            // Set email
            binding.tvProfileEmail.text = user.email ?: getString(R.string.no_email)
            
            // Set user ID
            binding.tvProfileUserId.text = getString(R.string.user_id_label, user.uid)
            
            // Set display name if available
            user.displayName?.let { displayName ->
                binding.tvProfileDisplayName.text = getString(R.string.name_label, displayName)
                binding.tvProfileDisplayName.visibility = android.view.View.VISIBLE
            }
            
            // Set account creation date
            user.metadata?.creationTimestamp?.let { timestamp ->
                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                val createdDate = Date(timestamp)
                binding.tvAccountCreated.text = getString(R.string.account_created_label, dateFormat.format(createdDate))
            }
            
            // Set last sign in date
            user.metadata?.lastSignInTimestamp?.let { timestamp ->
                val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                val lastLoginDate = Date(timestamp)
                binding.tvLastLogin.text = getString(R.string.last_login_label, dateFormat.format(lastLoginDate))
            }
            
            // Track profile view
            val properties = org.json.JSONObject().apply {
                put("user_id", user.uid)
                put("email", user.email ?: "")
            }
            mixpanel.track("Profile Viewed", properties)
        } else {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    
    private fun setupBackButton() {
        binding.btnBack.setOnClickListener {
            mixpanel.track("Profile Back Button Clicked")
            finish()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        mixpanel.flush()
    }
}
