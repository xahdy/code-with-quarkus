package org.acme.api

import io.quarkus.test.InjectMock
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import kotlinx.coroutines.runBlocking
import org.acme.domain.Product
import org.acme.domain.ProductRepository
import org.bson.types.ObjectId
import org.hamcrest.CoreMatchers.`is`
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.kotlin.anyOrNull

@QuarkusTest
class ProductResourceTest {

    @InjectMock
    lateinit var productRepository: ProductRepository

    @Test
    fun `test create product endpoint`() {
        val productToCreate = Product(name = "Test Product", description = "Test Description", price = 10.99)
        val createdProduct = Product(id = ObjectId(), name = "Test Product", description = "Test Description", price = 10.99)

        runBlocking {
            `when`(productRepository.create(anyOrNull())).thenReturn(createdProduct)
        }

        given()
            .contentType(ContentType.JSON)
            .body(productToCreate)
            .`when`()
            .post("/products")
            .then()
            .statusCode(201)
            .body("name", `is`("Test Product"))
            .body("description", `is`("Test Description"))
            .body("price", `is`(10.99f)) // RestAssured parses numbers as float by default
            .body("id", `is`(createdProduct.id.toString()))
    }

    @Test
    fun `test get all products endpoint`() {
        val products = listOf(
            Product(id = ObjectId(), name = "Product 1", description = "Description 1", price = 10.0),
            Product(id = ObjectId(), name = "Product 2", description = "Description 2", price = 20.0)
        )

        runBlocking {
            `when`(productRepository.findAll()).thenReturn(products)
        }

        given()
            .`when`()
            .get("/products")
            .then()
            .statusCode(200)
            .body("size()", `is`(2))
            .body("[0].name", `is`("Product 1"))
            .body("[1].name", `is`("Product 2"))
            .body("[0].id", `is`(products[0].id.toString()))
            .body("[1].id", `is`(products[1].id.toString()))
    }
}
