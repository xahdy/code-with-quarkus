package org.acme.api

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.acme.domain.Product
import org.acme.domain.ProductRepository

@Path("/products")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class ProductResource @Inject constructor(private val productRepository: ProductRepository) {

    @POST
    suspend fun createProduct(product: Product): Response {
        val createdProduct = productRepository.create(product)
        return Response.status(Response.Status.CREATED).entity(createdProduct).build()
    }

    @GET
    suspend fun getAllProducts(): List<Product> {
        return productRepository.findAll()
    }
}
