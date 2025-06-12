package org.acme.infra


import com.mongodb.kotlin.client.coroutine.MongoDatabase
import jakarta.enterprise.context.Dependent
import kotlinx.coroutines.flow.toList
import org.acme.domain.Product
import org.acme.domain.ProductRepository
import org.bson.types.ObjectId

@Dependent
class ProductRepositoryImpl(
    val database: MongoDatabase,
) : ProductRepository {
    private val collection = database.getCollection<Product>("products")

    override suspend fun create(product: Product): Product {
        val productWithId = product.copy(id = ObjectId()) // Ensure ID is generated before insert
        collection.insertOne(productWithId)
        return productWithId
    }

    override suspend fun findAll(): List<Product> {
        return collection.find().toList()
    }
}
