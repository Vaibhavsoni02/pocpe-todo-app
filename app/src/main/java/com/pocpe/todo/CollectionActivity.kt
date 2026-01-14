package com.pocpe.todo

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pocpe.todo.data.SampleProducts
import com.pocpe.todo.databinding.ActivityCollectionBinding
import com.pocpe.todo.models.CartItem
import com.pocpe.todo.models.CartManager
import com.pocpe.todo.models.Product
import com.pocpe.todo.utils.AnalyticsHelper

class CollectionActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityCollectionBinding
    private lateinit var productsAdapter: ProductsAdapter
    private val products = SampleProducts.getProducts()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCollectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        supportActionBar?.title = getString(R.string.collection)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        // Track screen view in RudderStack
        AnalyticsHelper.screen("Collection Screen", mapOf(
            "product_count" to products.size
        ))
        
        setupRecyclerView()
        setupViewCartButton()
    }
    
    private fun setupRecyclerView() {
        productsAdapter = ProductsAdapter(products) { product ->
            // Navigate to product detail
            val intent = Intent(this, ProductDetailActivity::class.java)
            intent.putExtra("product_id", product.id)
            startActivity(intent)
            
            // Track product clicked
            AnalyticsHelper.trackEvent("Product Clicked", mapOf(
                "product_id" to product.id,
                "product_name" to product.name,
                "category" to product.category
            ))
        }
        
        binding.rvProducts.layoutManager = LinearLayoutManager(this)
        binding.rvProducts.adapter = productsAdapter
    }
    
    private fun setupViewCartButton() {
        binding.btnViewCart.setOnClickListener {
            val intent = Intent(this, CartActivity::class.java)
            startActivity(intent)
            AnalyticsHelper.trackEvent("Cart Button Clicked")
        }
        
        // Update cart button text with item count
        updateCartButton()
    }
    
    private fun updateCartButton() {
        val itemCount = CartManager.getItemCount()
        if (itemCount > 0) {
            binding.btnViewCart.text = "${getString(R.string.cart)} ($itemCount)"
        } else {
            binding.btnViewCart.text = getString(R.string.cart)
        }
    }
    
    override fun onResume() {
        super.onResume()
        updateCartButton()
        productsAdapter.notifyDataSetChanged()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
    
    // Products RecyclerView Adapter
    private inner class ProductsAdapter(
        private val products: List<Product>,
        private val onProductClick: (Product) -> Unit
    ) : RecyclerView.Adapter<ProductsAdapter.ProductViewHolder>() {
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_product, parent, false)
            return ProductViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
            val product = products[position]
            holder.bind(product)
        }
        
        override fun getItemCount() = products.size
        
        inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val tvProductName: TextView = itemView.findViewById(R.id.tvProductName)
            private val tvProductDescription: TextView = itemView.findViewById(R.id.tvProductDescription)
            private val tvProductPrice: TextView = itemView.findViewById(R.id.tvProductPrice)
            private val btnAddToCart: Button = itemView.findViewById(R.id.btnAddToCart)
            
            fun bind(product: Product) {
                tvProductName.text = product.name
                tvProductDescription.text = product.description
                tvProductPrice.text = "${product.currency} ${product.price}"
                
                // Check if product is already in cart
                val cartItems = CartManager.getCartItems()
                val inCart = cartItems.any { it.product.id == product.id }
                
                if (inCart) {
                    btnAddToCart.text = getString(R.string.add_to_cart) + " (In Cart)"
                    btnAddToCart.isEnabled = false
                } else {
                    btnAddToCart.text = getString(R.string.add_to_cart)
                    btnAddToCart.isEnabled = true
                }
                
                // Product click - navigate to detail
                itemView.setOnClickListener {
                    onProductClick(product)
                }
                
                // Add to cart click
                btnAddToCart.setOnClickListener {
                    CartManager.addToCart(product, 1)
                    Toast.makeText(itemView.context, "${product.name} added to cart", Toast.LENGTH_SHORT).show()
                    
                    // Track add to cart in RudderStack
                    AnalyticsHelper.trackProductAdded(product, 1)
                    
                    // Update UI
                    notifyDataSetChanged()
                    updateCartButton()
                }
            }
        }
    }
}
