package org.acme.api

import io.quarkus.test.junit.QuarkusIntegrationTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.acme.domain.Product
import org.bson.types.ObjectId // Import ObjectId
import org.hamcrest.CoreMatchers.* // For RestAssured body checks
import org.junit.jupiter.api.Test

@QuarkusIntegrationTest
class ProductResourceIT {

    // Helper to make a Product object with a specific or new ObjectId
    private fun sampleProductWithId(id: ObjectId = ObjectId(), name: String, description: String, price: Double): Product {
        return Product(id = id, name = name, description = description, price = price)
    }

    @Test
    fun testCreateProduct_verifyNameAndPrice() {
        val predefinedId = ObjectId()
        val productPayload = sampleProductWithId(predefinedId, "Client ID Create", "Desc Create", 12.34)

        given()
            .contentType(ContentType.JSON)
            .body(productPayload) // Product object with predefined ObjectId
        .`when`()
            .post("/products")
        .then()
            .statusCode(201) // Assertion 1
            .body("name", equalTo(productPayload.name)) // Assertion 2
            .body("price", equalTo(productPayload.price.toFloat())) // Assertion 3
            // ID check would be .body("id", equalTo(predefinedId.toHexString())) but that's a 4th assert
    }

    @Test
    fun testCreateProduct_verifyId() {
        val predefinedId = ObjectId()
        val productPayload = sampleProductWithId(predefinedId, "Client ID Create", "Desc Create", 12.34)

        given()
            .contentType(ContentType.JSON)
            .body(productPayload)
        .`when`()
            .post("/products")
        .then()
            .statusCode(201) // Assertion 1
            .body("id", equalTo(predefinedId.toHexString())) // Assertion 2
            // Max 1 more assertion possible here
    }

    @Test
    fun testGetAllProducts_findsCreated() {
        val product1Id = ObjectId()
        val product1Payload = sampleProductWithId(product1Id, "P1 For Get", "D1", 1.0)
        given().contentType(ContentType.JSON).body(product1Payload).post("/products").then().statusCode(201)

        val product2Id = ObjectId()
        val product2Payload = sampleProductWithId(product2Id, "P2 For Get", "D2", 2.0)
        given().contentType(ContentType.JSON).body(product2Payload).post("/products").then().statusCode(201)

        given()
        .`when`()
            .get("/products")
        .then()
            .statusCode(200) // Assertion 1
            .body("find { it.id == '${product1Id.toHexString()}' }.name", equalTo(product1Payload.name)) // Assertion 2
            .body("find { it.id == '${product2Id.toHexString()}' }.name", equalTo(product2Payload.name)) // Assertion 3
    }


    @Test
    fun testUpdateProduct_verifyNameAndPrice() {
        val originalId = ObjectId()
        val originalPayload = sampleProductWithId(originalId, "Original For Update", "Orig Desc", 3.0)
        given().contentType(ContentType.JSON).body(originalPayload).post("/products").then().statusCode(201)

        val updatedName = "Updated Name"
        val updatedPrice = 3.5
        val updateDataPayload = Product( // Constructing with default ID, server uses path ID
            name = updatedName,
            description = originalPayload.description,
            price = updatedPrice
        )

        given()
            .contentType(ContentType.JSON)
            .body(updateDataPayload) // Server uses path ID primarily for which doc to update
        .`when`()
            .put("/products/${originalId.toHexString()}")
        .then()
            .statusCode(200) // Assertion 1
            .body("name", equalTo(updatedName)) // Assertion 2
            .body("price", equalTo(updatedPrice.toFloat())) // Assertion 3
    }

    @Test
    fun testUpdateProduct_verifyIdUnchanged() {
        val originalId = ObjectId()
        val originalPayload = sampleProductWithId(originalId, "Original For Update ID Test", "Orig Desc", 3.0)
        given().contentType(ContentType.JSON).body(originalPayload).post("/products").then().statusCode(201)

        val updateDataPayload = Product(name = "Name Change", description = originalPayload.description, price = 3.5)

        given()
            .contentType(ContentType.JSON)
            .body(updateDataPayload)
        .`when`()
            .put("/products/${originalId.toHexString()}")
        .then()
            .statusCode(200) // Assertion 1
            .body("id", equalTo(originalId.toHexString())); // Assertion 2
            // Max 1 more assertion
    }

    @Test
    fun testDeleteProduct_isGoneFromList() {
        val idToDelete = ObjectId()
        val productToDeletePayload = sampleProductWithId(idToDelete, "To Delete", "Del Desc", 4.0)
        given().contentType(ContentType.JSON).body(productToDeletePayload).post("/products").then().statusCode(201)

        given()
        .`when`()
            .delete("/products/${idToDelete.toHexString()}")
        .then()
            .statusCode(204) // Assertion 1

        // Verify it's gone from the list
        given()
        .`when`()
            .get("/products")
        .then()
            .statusCode(200) // Assertion 2
            .body("find { it.id == '${idToDelete.toHexString()}' }", is(nullValue())); // Assertion 3
    }
}
