package com.kuramapommel.til_akka_typed.domain.model

import scala.concurrent.{ExecutionContext, Future}
import cats.data.EitherT
import com.kuramapommel.til_akka_typed.domain.model.valueobject.*
import com.kuramapommel.til_akka_typed.domain.model.error.ProductError

/** 商品リポジトリ. */
trait ProductRepository:
  /** 商品IDによる商品検索.
    * @param id
    *   商品ID
    * @return
    *   商品エンティティ
    */
  def findById(id: ProductId): ExecutionContext ?=> EitherT[Future, ProductError, Product]

  /** 商品情報の保存.
    *
    * @param product
    *   商品エンティティ
    * @return
    *   商品ID
    */
  def save(product: Product): ExecutionContext ?=> EitherT[Future, ProductError, ProductId]
