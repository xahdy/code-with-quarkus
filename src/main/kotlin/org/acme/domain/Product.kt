package org.acme.domain

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class Product(
    @BsonId
    val id: ObjectId = ObjectId(),
    val name: String,
    val description: String,
    val price: Double
)
