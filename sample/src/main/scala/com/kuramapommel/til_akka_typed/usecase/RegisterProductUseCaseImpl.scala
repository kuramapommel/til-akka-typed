package com.kuramapommel.til_akka_typed.usecase

import cats.data.EitherT
import com.kuramapommel.til_akka_typed.domain.model.Product
import com.kuramapommel.til_akka_typed.domain.model.ProductIdGenerator
import com.kuramapommel.til_akka_typed.domain.model.ProductRepository
import com.kuramapommel.til_akka_typed.domain.model.error.ProductError
import com.kuramapommel.til_akka_typed.domain.model.event.ProductEvent
import com.kuramapommel.til_akka_typed.domain.model.valueobject.*
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

/**
 * 商品登録ユースケース実装.
 *
 * @constructor
 *   商品登録ユースケース実装を生成する.
 * @param productIdGenerator
 *   商品IDジェネレータ
 * @param productRepository
 *   商品リポジトリ
 */
class RegisterProductUseCaseImpl(
    productIdGenerator: ProductIdGenerator,
    productRepository: ProductRepository
) extends RegisterProductUseCase:

  override def execute(name: String, imageUrl: ImageURL, price: Int, description: String)(
      eventPublisher: ProductEvent => Unit
  ): ExecutionContext ?=> EitherT[Future, ProductError, Unit] =
    for
      product <- productIdGenerator
        .generate()
        .map: id =>
          Product(id, name, imageUrl, price, description)
      savedId <- productRepository.save(product)
    yield eventPublisher(
      ProductEvent.Registered(savedId, product.name, product.imageUrl, product.price, product.description)
    )
