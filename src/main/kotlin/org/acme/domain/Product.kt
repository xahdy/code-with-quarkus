package org.acme.domain

import org.bson.types.ObjectId

data class Product(
    val id: ObjectId? = null,
    val name: String,
    val description: String,
    val price: Double
)
