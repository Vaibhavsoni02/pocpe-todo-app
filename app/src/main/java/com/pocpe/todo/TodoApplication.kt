package com.pocpe.todo

import android.app.Application
import com.pocpe.todo.utils.RudderStackHelper

class TodoApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize RudderStack SDK
        RudderStackHelper.initialize(this)
        
        // Identify user if already logged in
        // This will be called after login in MainActivity
    }
}
