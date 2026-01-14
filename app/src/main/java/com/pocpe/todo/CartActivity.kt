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
import com.pocpe.todo.databinding.ActivityCartBinding
import com.pocpe.todo.models.CartItem
import com.pocpe.todo.models.CartManager
import com.pocpe.todo.utils.AnalyticsHelper

class CartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCartBinding
    private lateinit var adapter: CartAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = getString(R.string.cart)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        AnalyticsHelper.screen("Cart Screen", mapOf("item_count" to CartManager.getItemCount()))

        adapter = CartAdapter(CartManager.getCartItems().toMutableList())
        binding.rvCartItems.layoutManager = LinearLayoutManager(this)
        binding.rvCartItems.adapter = adapter

        binding.btnCheckout.setOnClickListener {
            if (CartManager.getCartItems().isEmpty()) {
                Toast.makeText(this, getString(R.string.empty_cart), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            AnalyticsHelper.trackEvent("Proceed to Checkout Clicked", mapOf("total" to CartManager.getCartTotal()))
            startActivity(Intent(this, CheckoutActivity::class.java))
        }

        refreshUi()
    }

    override fun onResume() {
        super.onResume()
        adapter.setItems(CartManager.getCartItems().toMutableList())
        refreshUi()
    }

    private fun refreshUi() {
        val items = CartManager.getCartItems()
        val isEmpty = items.isEmpty()
        binding.tvEmptyCart.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.rvCartItems.visibility = if (isEmpty) View.GONE else View.VISIBLE
        binding.btnCheckout.isEnabled = !isEmpty

        val total = CartManager.getCartTotal()
        binding.tvCartTotal.text = "${getString(R.string.total)}: USD ${"%.2f".format(total)}"
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private inner class CartAdapter(
        private var items: MutableList<CartItem>
    ) : RecyclerView.Adapter<CartAdapter.CartVH>() {

        fun setItems(newItems: MutableList<CartItem>) {
            items = newItems
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartVH {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cart, parent, false)
            return CartVH(view)
        }

        override fun onBindViewHolder(holder: CartVH, position: Int) {
            holder.bind(items[position])
        }

        override fun getItemCount(): Int = items.size

        inner class CartVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val tvName: TextView = itemView.findViewById(R.id.tvCartProductName)
            private val tvPrice: TextView = itemView.findViewById(R.id.tvCartProductPrice)
            private val tvQty: TextView = itemView.findViewById(R.id.tvCartQuantity)
            private val btnRemove: Button = itemView.findViewById(R.id.btnRemoveFromCart)

            fun bind(cartItem: CartItem) {
                tvName.text = cartItem.product.name
                tvPrice.text = "${cartItem.product.currency} ${"%.2f".format(cartItem.product.price)}"
                tvQty.text = "${getString(R.string.quantity)}: ${cartItem.quantity}"

                btnRemove.setOnClickListener {
                    CartManager.removeFromCart(cartItem.product.id)
                    AnalyticsHelper.trackEvent("Product Removed", mapOf("product_id" to cartItem.product.id))
                    setItems(CartManager.getCartItems().toMutableList())
                    refreshUi()
                }
            }
        }
    }
}

