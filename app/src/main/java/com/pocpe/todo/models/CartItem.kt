package com.pocpe.todo.models

import java.io.Serializable

data class CartItem(
    val product: Product,
    var quantity: Int = 1
) : Serializable {
    fun getTotalPrice(): Double {
        return product.price * quantity
    }
}

object CartManager {
    private val cartItems = mutableListOf<CartItem>()
    
    fun addToCart(product: Product, quantity: Int = 1) {
        val existingItem = cartItems.find { it.product.id == product.id }
        if (existingItem != null) {
            existingItem.quantity += quantity
        } else {
            cartItems.add(CartItem(product, quantity))
        }
    }
    
    fun removeFromCart(productId: String) {
        cartItems.removeAll { it.product.id == productId }
    }
    
    fun updateQuantity(productId: String, quantity: Int) {
        val item = cartItems.find { it.product.id == productId }
        item?.quantity = quantity
        if (item != null && item.quantity <= 0) {
            removeFromCart(productId)
        }
    }
    
    fun getCartItems(): List<CartItem> {
        return cartItems.toList()
    }
    
    fun getCartTotal(): Double {
        return cartItems.sumOf { it.getTotalPrice() }
    }
    
    fun getItemCount(): Int {
        return cartItems.sumOf { it.quantity }
    }
    
    fun clearCart() {
        cartItems.clear()
    }
}
