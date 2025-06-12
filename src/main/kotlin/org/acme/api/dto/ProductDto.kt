package org.acme.api.dto

import org.acme.domain.Product // Required for the factory method

data class ProductDto(
    val id: String,
    val name: String,
    val description: String,
    val price: Double
) {
    companion object {
        fun fromProduct(product: Product): ProductDto {
            return ProductDto(
                id = product.id.toHexString(),
                name = product.name,
                description = product.description,
                price = product.price
            )
        }
    }
}
