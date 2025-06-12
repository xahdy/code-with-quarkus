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
import org.acme.api.dto.ProductDto // New import
import org.acme.domain.Product
import org.acme.domain.ProductRepository
import org.bson.types.ObjectId

@Path("/products")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class ProductResource (private val productRepository: ProductRepository) {

    @POST
    suspend fun createProduct(product: Product): Response { // Input is still Product
        val createdDomainProduct = productRepository.create(product)
        val productDto = ProductDto.fromProduct(createdDomainProduct)
        return Response.status(Response.Status.CREATED).entity(productDto).build()
    }

    @GET
    suspend fun getAllProducts(): List<ProductDto> {
        val domainProducts = productRepository.findAll()
        return domainProducts.map { ProductDto.fromProduct(it) }
    }

    @PUT
    @Path("/{id}")
    suspend fun updateProduct(@PathParam("id") id: String, productData: Product): Response { // Input is Product
        // Ensure ID from path is used, productData might have a dummy or incorrect ObjectId
        val productToUpdate = productData.copy(id = ObjectId(id))
        val updatedDomainProduct = productRepository.update(productToUpdate)
        return if (updatedDomainProduct != null) {
            val productDto = ProductDto.fromProduct(updatedDomainProduct)
            Response.ok(productDto).build()
        } else {
            Response.status(Response.Status.NOT_FOUND).build()
        }
    }

    @DELETE
    @Path("/{id}")
    suspend fun deleteProduct(@PathParam("id") id: String): Response { // Parameter name changed in prompt, but @PathParam already uses "id"
        return if (productRepository.delete(id)) {
            Response.noContent().build()
        } else {
            Response.status(Response.Status.NOT_FOUND).build()
        }
    }
}
