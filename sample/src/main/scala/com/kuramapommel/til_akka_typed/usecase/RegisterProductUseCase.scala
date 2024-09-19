package com.kuramapommel.til_akka_typed.usecase

import scala.concurrent.{ExecutionContext, Future}
import cats.data.EitherT
import com.kuramapommel.til_akka_typed.domain.model.valueobject.ImageURL
import com.kuramapommel.til_akka_typed.domain.model.error.ProductError
import com.kuramapommel.til_akka_typed.domain.model.event.ProductEvent

/** 商品登録ユースケース. */
trait RegisterProductUseCase:

  /** 商品登録.
    * @param name
    *   商品名
    * @param imageUrl
    *   商品画像URL
    * @param price
    *   価格
    * @param description
    *   商品説明
    * @param eventPublisher
    *   イベントパブリッシャー
    * @return
    *   商品登録結果
    */
  def execute(name: String, imageUrl: ImageURL, price: Int, description: String)(
      eventPublisher: ProductEvent => Unit
  ): ExecutionContext ?=> EitherT[Future, ProductError, Unit]
