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

    override suspend fun update(product: Product): Product? {
        val updateResult = collection.replaceOneById(product.id!!, product)
        return if (updateResult.modifiedCount > 0) product else null
    }

    override suspend fun delete(id: String): Boolean {
        val objectId = ObjectId(id)
        val deleteResult = collection.deleteOneById(objectId)
        return deleteResult.deletedCount > 0
    }
}
