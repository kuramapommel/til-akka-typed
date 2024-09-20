package com.kuramapommel.til_akka_typed.usecase

import com.kuramapommel.til_akka_typed.domain.model.event.ProductEvent
import scala.concurrent.ExecutionContext
import cats.data.EitherT
import scala.concurrent.Future
import com.kuramapommel.til_akka_typed.domain.model.error.ProductError
import com.kuramapommel.til_akka_typed.domain.model.valueobject._

/** 商品編集ユースケース. */
trait EditProductUseCase:

  /** 商品編集.
    *
    * @param id
    *   商品ID
    * @param nameOpt
    *   商品名
    * @param imageUrlOpt
    *   商品画像URL
    * @param priceOpt
    *   価格
    * @param descriptionOpt
    *   商品説明
    * @param eventPublisher
    *   イベントパブリッシャー
    * @return
    *   商品編集結果
    */
  def execute(
      id: String,
      nameOpt: Option[String],
      imageUrlOpt: Option[ImageURL],
      priceOpt: Option[Int],
      descriptionOpt: Option[String]
  )(
      eventPublisher: ProductEvent => Unit
  ): ExecutionContext ?=> EitherT[Future, ProductError, Unit]
