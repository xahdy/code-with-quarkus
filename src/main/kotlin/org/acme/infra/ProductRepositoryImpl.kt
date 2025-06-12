package org.acme.infra


import com.mongodb.client.model.Filters
import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.ReturnDocument
import com.mongodb.client.model.Updates
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
        // Assumes product.id is already set (e.g., by Product's default constructor if not provided by client,
        // or uses the client-provided ObjectId if it was set in the input 'product' object)
        collection.insertOne(product)
        return product
    }

    override suspend fun findAll(): List<Product> {
        return collection.find().toList()
    }

    override suspend fun update(product: Product): Product? {
        val filter = Filters.eq("_id", product.id!!)
        val updates = Updates.combine(
            Updates.set("name", product.name),
            Updates.set("description", product.description),
            Updates.set("price", product.price)
        )
        val options = FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
        return collection.findOneAndUpdate(filter, updates, options)
    }

    override suspend fun delete(id: String): Boolean {
        val objectId = ObjectId(id)
        val deleteResult = collection.deleteOne(Filters.eq("_id", objectId))
        return deleteResult.deletedCount > 0
    }
}
