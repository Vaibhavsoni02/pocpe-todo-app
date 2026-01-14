package com.pocpe.todo.utils

import android.app.Application
import com.rudderstack.android.sdk.core.RudderClient
import com.rudderstack.android.sdk.core.RudderConfig
import com.rudderstack.android.sdk.core.RudderLogger

object RudderStackHelper {
    private var rudderClient: RudderClient? = null
    
    fun initialize(application: Application) {
        if (rudderClient == null) {
            rudderClient = RudderClient.getInstance(
                application,
                "385E66r2BSN3XU9yWNI90AfcT8K",
                RudderConfig.Builder()
                    .withDataPlaneUrl("https://pocpetvwpvrdbn.dataplane.rudderstack.com")
                    .withLogLevel(RudderLogger.RudderLogLevel.DEBUG)
                    .build()
            )
        }
    }
    
    fun getClient(): RudderClient? {
        return rudderClient
    }
}
