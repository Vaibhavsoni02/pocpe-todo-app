package com.pocpe.todo.data

import com.pocpe.todo.models.Product

object SampleProducts {
    fun getProducts(): List<Product> {
        return listOf(
            Product(
                id = "prod_001",
                name = "Wireless Headphones",
                description = "Premium wireless headphones with noise cancellation",
                price = 99.99,
                currency = "USD",
                imageUrl = "",
                category = "Electronics",
                sku = "WH-001",
                brand = "TechBrand"
            ),
            Product(
                id = "prod_002",
                name = "Smart Watch",
                description = "Fitness tracking smartwatch with heart rate monitor",
                price = 199.99,
                currency = "USD",
                imageUrl = "",
                category = "Electronics",
                sku = "SW-002",
                brand = "TechBrand"
            ),
            Product(
                id = "prod_003",
                name = "Wireless Mouse",
                description = "Ergonomic wireless mouse with long battery life",
                price = 29.99,
                currency = "USD",
                imageUrl = "",
                category = "Accessories",
                sku = "WM-003",
                brand = "TechBrand"
            ),
            Product(
                id = "prod_004",
                name = "USB-C Cable",
                description = "Fast charging USB-C cable, 6ft length",
                price = 12.99,
                currency = "USD",
                imageUrl = "",
                category = "Accessories",
                sku = "UC-004",
                brand = "TechBrand"
            ),
            Product(
                id = "prod_005",
                name = "Laptop Stand",
                description = "Adjustable aluminum laptop stand",
                price = 49.99,
                currency = "USD",
                imageUrl = "",
                category = "Accessories",
                sku = "LS-005",
                brand = "TechBrand"
            ),
            Product(
                id = "prod_006",
                name = "Mechanical Keyboard",
                description = "RGB mechanical keyboard with blue switches",
                price = 79.99,
                currency = "USD",
                imageUrl = "",
                category = "Accessories",
                sku = "MK-006",
                brand = "TechBrand"
            )
        )
    }
    
    fun getProductById(id: String): Product? {
        return getProducts().find { it.id == id }
    }
}
