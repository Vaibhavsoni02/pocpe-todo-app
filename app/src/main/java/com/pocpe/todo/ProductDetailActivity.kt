package com.pocpe.todo

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.pocpe.todo.data.SampleProducts
import com.pocpe.todo.databinding.ActivityProductDetailBinding
import com.pocpe.todo.models.CartManager
import com.pocpe.todo.utils.AnalyticsHelper

class ProductDetailActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityProductDetailBinding
    private var product: com.pocpe.todo.models.Product? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        val productId = intent.getStringExtra("product_id")
        product = productId?.let { SampleProducts.getProductById(it) }
        
        if (product == null) {
            Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        // Track product viewed in RudderStack
        AnalyticsHelper.trackProductViewed(product!!)
        
        setupProductDetails()
        setupAddToCartButton()
    }
    
    private fun setupProductDetails() {
        product?.let { p ->
            binding.tvProductName.text = p.name
            binding.tvProductPrice.text = "${p.currency} ${p.price}"
            binding.tvProductCategory.text = p.category
            binding.tvProductDescription.text = p.description
            binding.tvProductSku.text = "SKU: ${p.sku}"
            
            supportActionBar?.title = p.name
        }
    }
    
    private fun setupAddToCartButton() {
        product?.let { p ->
            val cartItems = CartManager.getCartItems()
            val inCart = cartItems.any { it.product.id == p.id }
            
            if (inCart) {
                binding.btnAddToCart.text = "${getString(R.string.add_to_cart)} (In Cart)"
                binding.btnAddToCart.isEnabled = false
            } else {
                binding.btnAddToCart.text = getString(R.string.add_to_cart)
                binding.btnAddToCart.isEnabled = true
            }
            
            binding.btnAddToCart.setOnClickListener {
                CartManager.addToCart(p, 1)
                Toast.makeText(this, "${p.name} added to cart", Toast.LENGTH_SHORT).show()
                
                // Track add to cart in RudderStack
                AnalyticsHelper.trackProductAdded(p, 1)
                
                // Update button
                binding.btnAddToCart.text = "${getString(R.string.add_to_cart)} (In Cart)"
                binding.btnAddToCart.isEnabled = false
            }
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
