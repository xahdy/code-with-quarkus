package org.acme.api

import io.quarkus.test.junit.QuarkusIntegrationTest // Changed import
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.acme.domain.Product
// Removed ObjectId import as it's not directly used in test logic after companion object removal
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
            .body(newProduct)
        .`when`()
            .post("/products")
        .then()
            .statusCode(201)
            .body("name", equalTo(newProduct.name))
            .body("description", equalTo(newProduct.description))
            .body("price", equalTo(newProduct.price.toFloat()))
            .body("id", notNullValue())
            .body("id", `is`(not(emptyString())))
        // Removed extraction and storing to companion object
    }

    // Original testGetAllProducts (will be refactored)
    @Test
    fun testGetAllProducts() {
        // Setup: Create a product to ensure the list isn't empty
        val product1Name = "Product A for GET"
        val product1 = Product(name = product1Name, description = "First product for GET test", price = 10.0)
        val createdProduct1 = given()
            .contentType(ContentType.JSON)
            .body(product1)
        .`when`()
            .post("/products")
        .then()
            .statusCode(201)
            .extract().`as`(Product::class.java)

        val product2Name = "Product B for GET"
        val product2 = Product(name = product2Name, description = "Second product for GET test", price = 20.0)
        val createdProduct2 = given()
            .contentType(ContentType.JSON)
            .body(product2)
        .`when`()
            .post("/products")
        .then()
            .statusCode(201)
            .extract().`as`(Product::class.java)

        // Act & Assert
        given()
        .`when`()
            .get("/products")
        .then()
            .statusCode(200)
            .body("$", not(empty<Any>())) // Check if the list is not empty
            // Verify Product 1
            .body("find { it.id == '${createdProduct1.id.toHexString()}' }.name", equalTo(createdProduct1.name))
            .body("find { it.id == '${createdProduct1.id.toHexString()}' }.description", equalTo(createdProduct1.description))
            .body("find { it.id == '${createdProduct1.id.toHexString()}' }.price", equalTo(createdProduct1.price.toFloat()))
            // Verify Product 2
            .body("find { it.id == '${createdProduct2.id.toHexString()}' }.name", equalTo(createdProduct2.name))
            .body("find { it.id == '${createdProduct2.id.toHexString()}' }.description", equalTo(createdProduct2.description))
            .body("find { it.id == '${createdProduct2.id.toHexString()}' }.price", equalTo(createdProduct2.price.toFloat()))
    }

    // Original testUpdateProduct (will be refactored)
    @Test
    fun testUpdateProduct() {
        // Setup: Create a product to update
        val originalProductPayload = Product(name = "Original Product Name", description = "Original Description", price = 50.0)
        val createdProduct = given()
            .contentType(ContentType.JSON)
            .body(originalProductPayload)
        .`when`()
            .post("/products")
        .then()
            .statusCode(201)
            .extract().`as`(Product::class.java)

        val createdProductId = createdProduct.id.toHexString()

        // Prepare updated data
        val updatedName = "Updated Product Name"
        val updatedPrice = 55.5
        val productUpdatePayload = Product(
            id = createdProduct.id, // The ID in the payload for PUT is often ignored by server if ID is in path
            name = updatedName,
            description = createdProduct.description, // Keep description the same
            price = updatedPrice
        )

        // Act & Assert: Update the product
        given()
            .contentType(ContentType.JSON)
            .body(productUpdatePayload)
        .`when`()
            .put("/products/${createdProductId}")
        .then()
            .statusCode(200)
            .body("id", equalTo(createdProductId))
            .body("name", equalTo(updatedName))
            .body("description", equalTo(createdProduct.description)) // Should be unchanged
            .body("price", equalTo(updatedPrice.toFloat()))

        // Optional: Further verify by fetching all products and checking the updated one
        // This is a good practice if you want to be absolutely sure beyond the PUT response.
        given()
        .`when`()
            .get("/products")
        .then()
            .statusCode(200)
            .body("find { it.id == '${createdProductId}' }.name", equalTo(updatedName))
            .body("find { it.id == '${createdProductId}' }.price", equalTo(updatedPrice.toFloat()))
    }

    // Original testDeleteProduct (will be refactored)
    @Test
    fun testDeleteProduct() {
        // Setup: Create a product to delete
        val productToDeletePayload = Product(name = "Product to Delete", description = "This product will be deleted", price = 10.0)
        val createdProduct = given()
            .contentType(ContentType.JSON)
            .body(productToDeletePayload)
        .`when`()
            .post("/products")
        .then()
            .statusCode(201)
            .extract().`as`(Product::class.java)

        val createdProductId = createdProduct.id.toHexString()

        // Act: Delete the product
        given()
        .`when`()
            .delete("/products/${createdProductId}")
        .then()
            .statusCode(204) // No Content

        // Assert: Verify the product is no longer in the list of all products
        given()
        .`when`()
            .get("/products")
        .then()
            .statusCode(200)
            .body("find { it.id == '${createdProductId}' }", nullValue()) // Check that an item with the ID is not found
    }
}
