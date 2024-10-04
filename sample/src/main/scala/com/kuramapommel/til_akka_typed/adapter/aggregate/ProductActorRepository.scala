package com.kuramapommel.til_akka_typed.adapter.aggregate

import cats.data.EitherT
import com.kuramapommel.til_akka_typed.domain.model.Product
import com.kuramapommel.til_akka_typed.domain.model.ProductRepository
import com.kuramapommel.til_akka_typed.domain.model.error.ProductError
import com.kuramapommel.til_akka_typed.domain.model.valueobject.*
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

/** 商品アクターリポジトリ. */
object ProductActorRepository:
  /**
   * 商品アクターリポジトリの生成.
   * @param productMaybe
   *   商品エンティティのオプション
   * @return
   *   商品アクターリポジトリ
   */
  def apply(productMaybe: Option[Product]): ProductRepository =
    new ProductRepository:
      override def findById(id: ProductId): ExecutionContext ?=> EitherT[Future, ProductError, Product] =
        productMaybe match
          case Some(product) if product.id == id =>
            EitherT.rightT[Future, ProductError](product)
          case _ =>
            EitherT.leftT[Future, Product](ProductError.NotFound)

      override def save(product: Product): ExecutionContext ?=> EitherT[Future, ProductError, ProductId] =
        EitherT.rightT[Future, ProductError](product.id)
