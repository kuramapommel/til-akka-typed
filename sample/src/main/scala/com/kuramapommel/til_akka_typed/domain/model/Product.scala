package com.kuramapommel.til_akka_typed.domain.model

final case class Product(id: ProductId, name: String, imageUrl: String, price: Int, description: String):
  def edit(
      nameOpt: Option[String],
      imageUrlOpt: Option[String],
      priceOpt: Option[Int],
      descriptionOpt: Option[String]
  ): Product =
    copy(
      name = nameOpt.getOrElse(name),
      imageUrl = imageUrlOpt.getOrElse(imageUrl),
      price = priceOpt.getOrElse(price),
      description = descriptionOpt.getOrElse(description)
    )
