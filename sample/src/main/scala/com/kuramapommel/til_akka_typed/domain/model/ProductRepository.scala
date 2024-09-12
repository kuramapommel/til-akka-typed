package com.kuramapommel.til_akka_typed.domain.model

import scala.concurrent.{ExecutionContext, Future}
import cats.data.EitherT
import com.kuramapommel.til_akka_typed.domain.model.error.ProductError

trait ProductRepository {
  def findById(id: ProductId)(implicit
      ec: ExecutionContext
  ): EitherT[Future, ProductError, Product]
  def save(product: Product)(implicit
      ec: ExecutionContext
  ): EitherT[Future, ProductError, ProductId]
}

object ProductRepository {
  def apply(
      findByIdImpl: ProductId => Product,
      saveImpl: Product => ProductId
  ): ProductRepository = new ProductRepository {
    def findById(id: ProductId)(implicit
        ec: ExecutionContext
    ): EitherT[Future, ProductError, Product] =
      EitherT.rightT[Future, ProductError](findByIdImpl(id))
    def save(product: Product)(implicit
        ec: ExecutionContext
    ): EitherT[Future, ProductError, ProductId] =
      EitherT.rightT[Future, ProductError](saveImpl(product))
  }

}
