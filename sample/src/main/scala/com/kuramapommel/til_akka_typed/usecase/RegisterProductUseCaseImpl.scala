package com.kuramapommel.til_akka_typed.usecase

import scala.concurrent.{ExecutionContext, Future}
import cats.data.EitherT
import com.kuramapommel.til_akka_typed.domain.model.{Product, ProductId, ProductIdGenerator, ProductRepository}
import com.kuramapommel.til_akka_typed.domain.model.event.{ProductEvent, Registered}
import com.kuramapommel.til_akka_typed.domain.model.error.ProductError

class RegisterProductUseCaseImpl(
    productIdGenerator: ProductIdGenerator,
    productRepository: ProductRepository
) {

  def execute(
      name: String,
      imageUrl: String,
      price: Int,
      description: String
  )(
      eventPublisher: ProductEvent => Unit
  )(implicit ec: ExecutionContext): EitherT[Future, ProductError, Unit] = for {
    product <- productIdGenerator
      .generate()
      .map(id => Product(id, name, imageUrl, price, description))
    savedId <- productRepository.save(product)
  } yield eventPublisher(
    Registered(
      savedId,
      product.name,
      product.imageUrl,
      product.price,
      product.description
    )
  )
}
