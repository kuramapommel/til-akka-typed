package com.kuramapommel.til_akka_typed.usecase

import scala.concurrent.{ExecutionContext, Future}
import cats.data.EitherT
import com.kuramapommel.til_akka_typed.domain.model.{ProductId, ProductRepository}
import com.kuramapommel.til_akka_typed.domain.model.error.ProductError
import com.kuramapommel.til_akka_typed.domain.model.event.ProductEvent

class EditProductUseCaseImpl(
    productRepository: ProductRepository
) extends EditProductUseCase:
  override def execute(
      id: String,
      nameOpt: Option[String],
      imageUrlOpt: Option[String],
      priceOpt: Option[Int],
      descriptionOpt: Option[String]
  )(
      eventPublisher: ProductEvent => Unit
  )(implicit ec: ExecutionContext): EitherT[Future, ProductError, Unit] =
    val productId = ProductId(id)
    for
      product <- productRepository
        .findById(productId)
        .map: product =>
          product.edit(nameOpt, imageUrlOpt, priceOpt, descriptionOpt)
      savedId <- productRepository.save(product)
    yield eventPublisher(
      ProductEvent.Edited(savedId, nameOpt, imageUrlOpt, priceOpt, descriptionOpt)
    )
