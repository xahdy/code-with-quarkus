package org.acme.infra

import com.mongodb.client.model.Filters
import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoCollection
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import kotlinx.coroutines.reactive.awaitFirstOrElse
import kotlinx.coroutines.reactive.awaitSingle
import org.acme.domain.Product
import org.acme.domain.ProductRepository
import org.bson.types.ObjectId

@ApplicationScoped
class ProductRepositoryImpl : ProductRepository {

    @Inject
    lateinit var mongoClient: MongoClient

    private val collection: MongoCollection<Product>
        get() = mongoClient.getDatabase("product_db").getCollection("products", Product::class.java)

    override suspend fun create(product: Product): Product {
        val productWithId = product.copy(id = ObjectId()) // Ensure ID is generated before insert
        collection.insertOne(productWithId).awaitSingle()
        return productWithId
    }

    override suspend fun findAll(): List<Product> {
        return collection.find().collect().asList().awaitFirstOrElse { emptyList() }
    }
}
