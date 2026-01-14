package com.pocpe.todo

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.pocpe.todo.databinding.ActivityCheckoutBinding
import com.pocpe.todo.models.CartManager
import com.pocpe.todo.utils.AnalyticsHelper
import java.util.UUID

class CheckoutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCheckoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = getString(R.string.checkout)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val items = CartManager.getCartItems()
        if (items.isEmpty()) {
            Toast.makeText(this, getString(R.string.empty_cart), Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val total = CartManager.getCartTotal()

        AnalyticsHelper.screen("Checkout Screen", mapOf("total" to total, "item_count" to CartManager.getItemCount()))
        AnalyticsHelper.trackCheckoutStarted(total = total, currency = "USD", products = items.map { it.product })

        binding.tvCheckoutSummary.text =
            "Items: ${CartManager.getItemCount()}\n" +
            "Subtotal: USD ${"%.2f".format(total)}"

        binding.tvCheckoutTotal.text = "${getString(R.string.total)}: USD ${"%.2f".format(total)}"

        binding.btnPlaceOrder.setOnClickListener {
            val orderId = "order_${UUID.randomUUID()}"
            val products = CartManager.getCartItems().map { it.product }

            // Track purchase/order completion (also maps to FB App Events via RudderStack cloud/device mode)
            AnalyticsHelper.trackOrderCompleted(orderId = orderId, total = total, currency = "USD", products = products)

            CartManager.clearCart()

            Toast.makeText(this, getString(R.string.order_complete), Toast.LENGTH_SHORT).show()

            // Return to collection after purchase
            val intent = Intent(this, CollectionActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

