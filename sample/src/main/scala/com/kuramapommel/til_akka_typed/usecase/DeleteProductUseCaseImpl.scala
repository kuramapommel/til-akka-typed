package com.kuramapommel.til_akka_typed.usecase

import cats.data.EitherT
import com.kuramapommel.til_akka_typed.domain.model.ProductRepository
import com.kuramapommel.til_akka_typed.domain.model.error.ProductError
import com.kuramapommel.til_akka_typed.domain.model.event.ProductEvent
import com.kuramapommel.til_akka_typed.domain.model.valueobject.*
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

/**
 * 商品削除ユースケース実装.
 * @constructor
 *   商品削除ユースケース実装を生成する.
 * @param productRepository
 *   商品リポジトリ
 */
class DeleteProductUseCaseImpl(
    productRepository: ProductRepository
) extends DeleteProductUseCase:

  override def execute(id: String)(
      eventPublisher: ProductEvent => Unit
  ): ExecutionContext ?=> EitherT[Future, ProductError, Unit] =
    val productId = ProductId(id)
    for
      product <- productRepository
        .findById(productId)
        .map: product =>
          product.delete()
      savedId <- productRepository.save(product)
    yield eventPublisher(
      ProductEvent.Deleted(savedId, product.deleted)
    )
