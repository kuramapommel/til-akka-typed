package com.kuramapommel.til_akka_typed.usecase

import scala.concurrent.{ExecutionContext, Future}
import cats.data.EitherT
import com.kuramapommel.til_akka_typed.domain.model.{ProductId, ProductRepository}
import com.kuramapommel.til_akka_typed.domain.model.error.ProductError
import com.kuramapommel.til_akka_typed.domain.model.event.ProductEvent

class EditProductUseCaseImpl(
    productRepository: ProductRepository
) extends EditProductUseCase:
  def execute(
      productId: ProductId,
      name: Option[String],
      imageUrl: Option[String],
      price: Option[Int],
      description: Option[String]
  )(eventPublisher: ProductEvent => Unit)(implicit ec: ExecutionContext): EitherT[Future, ProductError, Unit] =
    for
      product <- productRepository
        .findById(productId)
        .map: product =>
          product.edit(name, imageUrl, price, description)
      savedId <- productRepository.save(product)
    yield eventPublisher(
      ProductEvent.Edited(savedId, name, imageUrl, price, description)
    )
