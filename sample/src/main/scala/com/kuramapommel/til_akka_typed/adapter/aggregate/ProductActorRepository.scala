package com.kuramapommel.til_akka_typed.adapter.aggregate

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}
import akka.actor.typed.scaladsl.ActorContext
import cats.data.EitherT
import com.kuramapommel.til_akka_typed.domain.model.{Product, ProductId, ProductRepository}
import com.kuramapommel.til_akka_typed.domain.model.error.ProductError

object ProductActorRepository:
  def apply(productOpt: Option[Product], ctx: ActorContext[Command]): ProductRepository =
    new ProductRepository:
      def findById(id: ProductId): ExecutionContext ?=> EitherT[Future, ProductError, Product] =
        productOpt match
          case Some(product) if product.id == id =>
            EitherT.rightT[Future, ProductError](product)
          case _ =>
            EitherT.leftT[Future, Product](ProductError.NotFound)
      def save(product: Product): ExecutionContext ?=> EitherT[Future, ProductError, ProductId] =
        ctx.pipeToSelf(Future.successful(product)):
          case Success(product)   => Command.Store(product)
          case Failure(exception) => ???
        EitherT.rightT[Future, ProductError](product.id)
