package org.acme.api

import io.quarkus.test.junit.QuarkusIntegrationTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.acme.domain.Product
import org.acme.api.dto.ProductDto // New import
import org.hamcrest.CoreMatchers.*
import org.junit.jupiter.api.Test
// Removed MethodOrderer, Order, and TestMethodOrder imports

@QuarkusIntegrationTest // Changed annotation
class ProductResourceIT {

    // Original testCreateProduct (state saving removed)
    @Test
    fun testCreateProduct() {
        val newProduct = Product(name = "Test Product", description = "A product for testing", price = 99.99)

        given()
            .contentType(ContentType.JSON)
            .body(newProduct) // Send domain Product
        .`when`()
            .post("/products")
        .then()
            .statusCode(201)
            .body("name", equalTo(newProduct.name))
            .body("description", equalTo(newProduct.description))
            .body("price", equalTo(newProduct.price.toFloat()))
            .body("id", notNullValue()) // ID is a string in DTO
            .body("id", `is`(not(emptyString())))
            .extract().`as`(ProductDto::class.java) // Expect ProductDto
    }

    @Test
    fun testGetAllProducts() {
        // Setup: Create products to ensure the list isn't empty
        val product1 = Product(name = "Product A for GET", description = "First product for GET test", price = 10.0)
        val createdProduct1Dto = given()
            .contentType(ContentType.JSON)
            .body(product1) // Send domain Product
        .`when`()
            .post("/products")
        .then()
            .statusCode(201)
            .extract().`as`(ProductDto::class.java) // Expect ProductDto

        val product2 = Product(name = "Product B for GET", description = "Second product for GET test", price = 20.0)
        val createdProduct2Dto = given()
            .contentType(ContentType.JSON)
            .body(product2) // Send domain Product
        .`when`()
            .post("/products")
        .then()
            .statusCode(201)
            .extract().`as`(ProductDto::class.java) // Expect ProductDto

        // Act & Assert
        given()
        .`when`()
            .get("/products")
        .then()
            .statusCode(200)
            .body("$", not(empty<Any>()))
            // Verify Product 1
            .body("find { it.id == '${createdProduct1Dto.id}' }.name", equalTo(createdProduct1Dto.name))
            .body("find { it.id == '${createdProduct1Dto.id}' }.description", equalTo(createdProduct1Dto.description))
            .body("find { it.id == '${createdProduct1Dto.id}' }.price", equalTo(createdProduct1Dto.price.toFloat()))
            // Verify Product 2
            .body("find { it.id == '${createdProduct2Dto.id}' }.name", equalTo(createdProduct2Dto.name))
            .body("find { it.id == '${createdProduct2Dto.id}' }.description", equalTo(createdProduct2Dto.description))
            .body("find { it.id == '${createdProduct2Dto.id}' }.price", equalTo(createdProduct2Dto.price.toFloat()))
    }

    @Test
    fun testUpdateProduct() {
        // Setup: Create a product to update (response is ProductDto)
        val initialProductPayload = Product(name = "Original Name", description = "Original Desc", price = 50.0)
        val initialProductAsDto = given()
            .contentType(ContentType.JSON)
            .body(initialProductPayload) // Send domain Product
        .`when`()
            .post("/products")
        .then()
            .statusCode(201)
            .extract().`as`(ProductDto::class.java) // Expect ProductDto back

        val originalIdString = initialProductAsDto.id // This is already a String

        // Prepare updated data - sending a domain Product object still
        val updatedName = "Updated Product Name"
        val updatedPrice = 55.5
        // The Product sent in PUT body; its ID field is ignored by server due to path param taking precedence.
        val productUpdatePayload = Product(
            name = updatedName,
            description = initialProductAsDto.description, // Use description from DTO
            price = updatedPrice
            // id = ObjectId() // Default ObjectId is fine here, or omit if default is handled by data class
        )

        // Act & Assert: Update the product, expect ProductDto back
        given()
            .contentType(ContentType.JSON)
            .body(productUpdatePayload) // Send domain Product
        .`when`()
            .put("/products/${originalIdString}") // Use String ID in path
        .then()
            .statusCode(200)
            .body("id", equalTo(originalIdString))
            .body("name", equalTo(updatedName))
            .body("description", equalTo(initialProductAsDto.description))
            .body("price", equalTo(updatedPrice.toFloat()))
            .extract().`as`(ProductDto::class.java) // Expect ProductDto

        // Optional further verification
        given()
        .`when`()
            .get("/products")
        .then()
            .statusCode(200)
            .body("find { it.id == '${originalIdString}' }.name", equalTo(updatedName))
            .body("find { it.id == '${originalIdString}' }.price", equalTo(updatedPrice.toFloat()))
    }

    @Test
    fun testDeleteProduct() {
        // Setup: Create a product to delete
        val productToDeletePayload = Product(name = "Product to Delete", description = "This product will be deleted", price = 10.0)
        val createdProductDto = given()
            .contentType(ContentType.JSON)
            .body(productToDeletePayload) // Send domain Product
        .`when`()
            .post("/products")
        .then()
            .statusCode(201)
            .extract().`as`(ProductDto::class.java) // Expect ProductDto

        val createdProductIdString = createdProductDto.id // ID is already a String

        // Act: Delete the product
        given()
        .`when`()
            .delete("/products/${createdProductIdString}") // Use String ID
        .then()
            .statusCode(204) // No Content

        // Assert: Verify the product is no longer in the list of all products
        given()
        .`when`()
            .get("/products")
        .then()
            .statusCode(200)
            .body("find { it.id == '${createdProductIdString}' }", nullValue()) // Use String ID
    }
}
