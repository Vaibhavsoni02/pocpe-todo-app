package com.pocpe.todo.models

import java.io.Serializable

data class Product(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val currency: String = "USD",
    val imageUrl: String = "",
    val category: String = "",
    val sku: String = "",
    val brand: String = ""
) : Serializable
