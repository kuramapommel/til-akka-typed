package com.kuramapommel.til_akka_typed.usecase

import cats.data.EitherT
import com.kuramapommel.til_akka_typed.domain.model.error.ProductError
import com.kuramapommel.til_akka_typed.domain.model.event.ProductEvent
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

/** 商品削除ユースケース. */
trait DeleteProductUseCase:

  /**
   * 商品削除.
   *
   * @param id
   *   商品ID
   * @param eventPublisher
   *   イベントパブリッシャー
   * @return
   *   商品削除結果
   */
  def execute(id: String)(
      eventPublisher: ProductEvent => Unit
  ): ExecutionContext ?=> EitherT[Future, ProductError, Unit]
