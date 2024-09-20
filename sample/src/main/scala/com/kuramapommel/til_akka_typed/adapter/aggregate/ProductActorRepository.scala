package com.kuramapommel.til_akka_typed.adapter.aggregate

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}
import akka.actor.typed.scaladsl.ActorContext
import cats.data.EitherT
import com.kuramapommel.til_akka_typed.domain.model.{Product, ProductRepository}
import com.kuramapommel.til_akka_typed.domain.model.valueobject._
import com.kuramapommel.til_akka_typed.domain.model.error.ProductError

/** 商品アクターリポジトリ. */
object ProductActorRepository:
  /** 商品アクターリポジトリの生成.
    * @param productOpt
    *   商品エンティティのオプション
    * @param ctx
    *   アクターコンテキスト
    * @return
    *   商品アクターリポジトリ
    */
  def apply(productOpt: Option[Product], ctx: ActorContext[Command]): ProductRepository =
    new ProductRepository:
      override def findById(id: ProductId): ExecutionContext ?=> EitherT[Future, ProductError, Product] =
        productOpt match
          case Some(product) if product.id == id =>
            EitherT.rightT[Future, ProductError](product)
          case _ =>
            EitherT.leftT[Future, Product](ProductError.NotFound)

      override def save(product: Product): ExecutionContext ?=> EitherT[Future, ProductError, ProductId] =
        ctx.pipeToSelf(Future.successful(product)):
          case Success(product)   => Command.Store(product)
          case Failure(exception) => ???
        EitherT.rightT[Future, ProductError](product.id)
