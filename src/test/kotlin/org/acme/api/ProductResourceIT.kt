package org.acme.api

import io.quarkus.test.junit.QuarkusIntegrationTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.acme.domain.Product
import org.acme.api.dto.ProductDto
import org.assertj.core.api.Assertions.assertThat // AssertJ import
import org.junit.jupiter.api.Test

@QuarkusIntegrationTest
class ProductResourceIT {

    private fun createProductViaApi(product: Product): ProductDto {
        return given()
            .contentType(ContentType.JSON)
            .body(product)
        .`when`()
            .post("/products")
        .then()
            .statusCode(201)
            .extract().`as`(ProductDto::class.java)
    }

    @Test
    fun testCreateProduct() {
        val productToCreate = Product(name = "AssertJ Product", description = "Testing with AssertJ", price = 100.0)
        val createdProductDto = createProductViaApi(productToCreate)

        assertThat(createdProductDto.id).isNotNull().isNotEmpty()
        assertThat(createdProductDto.name).isEqualTo(productToCreate.name)
        assertThat(createdProductDto.description).isEqualTo(productToCreate.description)
        assertThat(createdProductDto.price).isEqualTo(productToCreate.price)
    }

    @Test
    fun testGetAllProducts() {
        val product1 = createProductViaApi(Product(name = "Product A for All", description = "Desc A", price = 10.0))
        val product2 = createProductViaApi(Product(name = "Product B for All", description = "Desc B", price = 20.0))

        val allProducts = given()
        .`when`()
            .get("/products")
        .then()
            .statusCode(200)
            .extract().`as`(Array<ProductDto>::class.java)

        assertThat(allProducts).isNotNull().isNotEmpty()
        assertThat(allProducts).extracting("id").contains(product1.id, product2.id)
        assertThat(allProducts).filteredOn { it.id == product1.id }.singleElement().satisfies {
            assertThat(it.name).isEqualTo(product1.name)
            assertThat(it.description).isEqualTo(product1.description)
        }
        assertThat(allProducts).filteredOn { it.id == product2.id }.singleElement().satisfies {
            assertThat(it.name).isEqualTo(product2.name)
        }
    }

    @Test
    fun testUpdateProduct() {
        val initialProductDto = createProductViaApi(Product(name = "Original for Update", description = "Original Desc", price = 50.0))

        val updatedName = "Updated Product Name"
        val updatedPrice = 55.5
        // Request body for PUT is still a Product domain object
        val productUpdatePayload = Product(
            name = updatedName,
            description = initialProductDto.description, // Keep original description
            price = updatedPrice
            // ID in payload is ignored by server, uses path ID
        )

        val updatedProductDto = given()
            .contentType(ContentType.JSON)
            .body(productUpdatePayload)
        .`when`()
            .put("/products/${initialProductDto.id}")
        .then()
            .statusCode(200)
            .extract().`as`(ProductDto::class.java)

        assertThat(updatedProductDto.id).isEqualTo(initialProductDto.id)
        assertThat(updatedProductDto.name).isEqualTo(updatedName)
        assertThat(updatedProductDto.description).isEqualTo(initialProductDto.description)
        assertThat(updatedProductDto.price).isEqualTo(updatedPrice)

        // Optional: Verify by getting all and checking
        val allProducts = given().get("/products").then().extract().`as`(Array<ProductDto>::class.java)
        assertThat(allProducts).filteredOn { it.id == initialProductDto.id }.singleElement().satisfies {
            assertThat(it.name).isEqualTo(updatedName)
        }
    }

    @Test
    fun testDeleteProduct() {
        val productToDeleteDto = createProductViaApi(Product(name = "Product to Delete", description = "Will be gone", price = 10.0))

        given()
        .`when`()
            .delete("/products/${productToDeleteDto.id}")
        .then()
            .statusCode(204)

        val productsAfterDelete = given()
        .`when`()
            .get("/products")
        .then()
            .statusCode(200)
            .extract().`as`(Array<ProductDto>::class.java)

        assertThat(productsAfterDelete).extracting("id").doesNotContain(productToDeleteDto.id)
    }
}
