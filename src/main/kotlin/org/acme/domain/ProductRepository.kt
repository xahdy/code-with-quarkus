package org.acme.domain

interface ProductRepository {
    suspend fun create(product: Product): Product
    suspend fun findAll(): List<Product>
}
