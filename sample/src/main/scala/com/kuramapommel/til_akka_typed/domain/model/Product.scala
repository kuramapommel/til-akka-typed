package com.kuramapommel.til_akka_typed.domain.model

final case class Product(id: ProductId, name: String, imageUrl: String, price: Int, description: String)
