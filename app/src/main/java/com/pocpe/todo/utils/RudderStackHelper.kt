package com.pocpe.todo.utils

import android.app.Application
import com.rudderstack.android.sdk.core.RudderClient
import com.rudderstack.android.sdk.core.RudderConfig
import com.rudderstack.android.sdk.core.RudderLogger

object RudderStackHelper {
    private var rudderClient: RudderClient? = null
    
    fun initialize(application: Application) {
        if (rudderClient == null) {
            val configBuilder = RudderConfig.Builder()
                .withDataPlaneUrl("https://pocpetvwpvrdbn.dataplane.rudderstack.com")
                .withLogLevel(RudderLogger.RudderLogLevel.DEBUG)

            // Enable Facebook App Events via RudderStack in DEVICE MODE.
            // We use reflection to avoid hard-coding the factory class name (varies across integration versions).
            tryAddDeviceModeFactory(
                configBuilder,
                listOf(
                    // Common naming patterns used by RudderStack Android integrations
                    "com.rudderstack.android.integrations.facebook.FacebookIntegrationFactory",
                    "com.rudderstack.android.integrations.facebook.FacebookFactory",
                    "com.rudderstack.android.integration.facebook.FacebookIntegrationFactory",
                    "com.rudderstack.android.integration.facebook.FacebookFactory",
                    "com.rudderstack.android.integrations.facebookapp.events.FacebookAppEventsFactory",
                    "com.rudderstack.android.integrations.facebookappevents.FacebookAppEventsFactory"
                )
            )

            rudderClient = RudderClient.getInstance(
                application,
                "385E66r2BSN3XU9yWNI90AfcT8K",
                configBuilder.build()
            )
        }
    }
    
    fun getClient(): RudderClient? {
        return rudderClient
    }

    private fun tryAddDeviceModeFactory(configBuilder: RudderConfig.Builder, classNames: List<String>) {
        val withFactory = configBuilder.javaClass.methods.firstOrNull {
            it.name == "withFactory" && it.parameterTypes.size == 1
        } ?: return

        for (className in classNames) {
            try {
                val cls = Class.forName(className)
                val factory = cls.getField("FACTORY").get(null)
                if (factory != null) {
                    withFactory.invoke(configBuilder, factory)
                    return
                }
            } catch (_: Throwable) {
                // ignore and try next
            }
        }
    }
}
