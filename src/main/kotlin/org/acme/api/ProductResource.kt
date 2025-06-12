package org.acme.api

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.DELETE
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.PUT
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.acme.domain.Product
import org.acme.domain.ProductRepository
import org.bson.types.ObjectId

@Path("/products")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class ProductResource (private val productRepository: ProductRepository) {

    @POST
    suspend fun createProduct(product: Product): Response {
        val createdProduct = productRepository.create(product)
        return Response.status(Response.Status.CREATED).entity(createdProduct).build()
    }

    @GET
    suspend fun getAllProducts(): List<Product> {
        return productRepository.findAll()
    }

    @PUT
    @Path("/{id}")
    suspend fun updateProduct(@PathParam("id") id: String, product: Product): Response {
        val objectId = ObjectId(id)
        val productToUpdate = product.copy(id = objectId)
        val updatedProduct = productRepository.update(productToUpdate)
        return if (updatedProduct != null) {
            Response.ok(updatedProduct).build()
        } else {
            Response.status(Response.Status.NOT_FOUND).build()
        }
    }

    @DELETE
    @Path("/{id}")
    suspend fun deleteProduct(@PathParam("id") id: String): Response {
        return if (productRepository.delete(id)) {
            Response.noContent().build()
        } else {
            Response.status(Response.Status.NOT_FOUND).build()
        }
    }
}
