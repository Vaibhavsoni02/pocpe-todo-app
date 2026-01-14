package com.pocpe.todo.utils

import com.pocpe.todo.models.Product
import com.rudderstack.android.sdk.core.RudderClient
import com.rudderstack.android.sdk.core.RudderProperty
import com.rudderstack.android.sdk.core.RudderTraits

object AnalyticsHelper {
    
    private fun getRudderClient(): RudderClient? {
        return RudderStackHelper.getClient()
    }
    
    fun trackEvent(eventName: String, properties: Map<String, Any>? = null) {
        val client = getRudderClient() ?: return

        val rudderProps = RudderProperty()
        properties?.forEach { (key, value) ->
            when (value) {
                is String -> rudderProps.putValue(key, value)
                is Int -> rudderProps.putValue(key, value)
                is Long -> rudderProps.putValue(key, value)
                is Double -> rudderProps.putValue(key, value)
                is Float -> rudderProps.putValue(key, value.toDouble())
                is Boolean -> rudderProps.putValue(key, value)
                else -> rudderProps.putValue(key, value.toString())
            }
        }

        client.track(eventName, rudderProps)
    }
    
    fun identify(userId: String, traits: Map<String, Any>? = null) {
        val client = getRudderClient() ?: return

        val rudderTraits = RudderTraits()
        traits?.forEach { (key, value) ->
            when (value) {
                is String -> rudderTraits.put(key, value)
                is Int -> rudderTraits.put(key, value)
                is Long -> rudderTraits.put(key, value)
                is Double -> rudderTraits.put(key, value)
                is Float -> rudderTraits.put(key, value.toDouble())
                is Boolean -> rudderTraits.put(key, value)
                else -> rudderTraits.put(key, value.toString())
            }
        }

        // RudderStack Android SDK (legacy) has multiple identify overloads across versions.
        // Use reflection so we stay compatible with the published version in CI.
        invokeIdentifyCompat(client, userId, rudderTraits)
    }

    private fun invokeIdentifyCompat(client: RudderClient, userId: String, traits: RudderTraits) {
        val methods = client.javaClass.methods.filter { it.name == "identify" }

        // 1) identify(String, RudderTraits, RudderOption?)
        methods.firstOrNull {
            it.parameterTypes.size == 3 &&
                it.parameterTypes[0] == String::class.java &&
                it.parameterTypes[1].name.endsWith("RudderTraits")
        }?.let {
            try {
                it.invoke(client, userId, traits, null)
                return
            } catch (_: Throwable) { /* ignore */ }
        }

        // 2) identify(String, RudderTraits)
        methods.firstOrNull {
            it.parameterTypes.size == 2 &&
                it.parameterTypes[0] == String::class.java &&
                it.parameterTypes[1].name.endsWith("RudderTraits")
        }?.let {
            try {
                it.invoke(client, userId, traits)
                return
            } catch (_: Throwable) { /* ignore */ }
        }

        // 3) identify(String) then identify(RudderTraits)
        methods.firstOrNull {
            it.parameterTypes.size == 1 && it.parameterTypes[0] == String::class.java
        }?.let {
            try { it.invoke(client, userId) } catch (_: Throwable) { /* ignore */ }
        }

        methods.firstOrNull {
            it.parameterTypes.size == 1 && it.parameterTypes[0].name.endsWith("RudderTraits")
        }?.let {
            try {
                it.invoke(client, traits)
                return
            } catch (_: Throwable) { /* ignore */ }
        }

        // 4) identify(RudderTraits, RudderOption?)
        methods.firstOrNull {
            it.parameterTypes.size == 2 && it.parameterTypes[0].name.endsWith("RudderTraits")
        }?.let {
            try {
                it.invoke(client, traits, null)
                return
            } catch (_: Throwable) { /* ignore */ }
        }
    }
    
    fun screen(screenName: String, properties: Map<String, Any>? = null) {
        val client = getRudderClient() ?: return

        val rudderProps = RudderProperty()
        properties?.forEach { (key, value) ->
            when (value) {
                is String -> rudderProps.putValue(key, value)
                is Int -> rudderProps.putValue(key, value)
                is Long -> rudderProps.putValue(key, value)
                is Double -> rudderProps.putValue(key, value)
                is Float -> rudderProps.putValue(key, value.toDouble())
                is Boolean -> rudderProps.putValue(key, value)
                else -> rudderProps.putValue(key, value.toString())
            }
        }

        client.screen(screenName, rudderProps)
    }
    
    // Ecommerce specific tracking methods
    
    fun trackProductViewed(product: Product) {
        trackEvent("Products Viewed", mapOf(
            "product_id" to product.id,
            "name" to product.name,
            "price" to product.price,
            "currency" to product.currency,
            "category" to product.category,
            "sku" to product.sku,
            "brand" to product.brand
        ))
    }
    
    fun trackProductAdded(product: Product, quantity: Int = 1) {
        trackEvent("Product Added", mapOf(
            "product_id" to product.id,
            "name" to product.name,
            "price" to product.price,
            "currency" to product.currency,
            "category" to product.category,
            "sku" to product.sku,
            "brand" to product.brand,
            "quantity" to quantity
        ))
    }
    
    fun trackCheckoutStarted(total: Double, currency: String = "USD", products: List<Product>) {
        trackEvent("Checkout Started", mapOf(
            "total" to total,
            "currency" to currency,
            "products" to products.joinToString(",") { it.id }
        ))
    }
    
    fun trackOrderCompleted(orderId: String, total: Double, currency: String = "USD", products: List<Product>) {
        trackEvent("Order Completed", mapOf(
            "order_id" to orderId,
            "revenue" to total,
            "total" to total,
            "currency" to currency,
            "products" to products.joinToString(",") { it.id }
        ))
        
        // Also track as Purchase for Facebook App Events
        trackPurchase(orderId, total, currency, products)
    }
    
    private fun trackPurchase(orderId: String, total: Double, currency: String, products: List<Product>) {
        // Facebook App Events expects "Purchase" event with revenue and currency
        trackEvent("Purchase", mapOf(
            "order_id" to orderId,
            "revenue" to total,
            "currency" to currency,
            "products" to products.joinToString(",") { it.id }
        ))
    }
}
