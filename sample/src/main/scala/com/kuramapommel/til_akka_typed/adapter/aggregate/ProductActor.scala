package com.kuramapommel.til_akka_typed.adapter.aggregate

import scala.concurrent.Promise
import scala.util.{Failure, Success}
import akka.actor.typed.{ActorRef, Behavior, DispatcherSelector}
import akka.actor.typed.scaladsl.Behaviors
import com.kuramapommel.til_akka_typed.domain.model.{Product, ProductId, ProductIdGenerator, ProductRepository}
import com.kuramapommel.til_akka_typed.domain.model.event.ProductEvent
import com.kuramapommel.til_akka_typed.usecase.RegisterProductUseCaseImpl
import com.kuramapommel.til_akka_typed.usecase.EditProductUseCaseImpl

object ProductActor:
  def apply(): Behavior[Command] =
    Behaviors.setup[Command]: (ctx) =>
      val productRepository = ProductActorRepository(None, ctx)
      active(productRepository)

  def active(productRepository: ProductRepository): Behavior[Command] =
    Behaviors.receive[Command]: (ctx, msg) =>
      import Command._
      implicit val executionContext =
        ctx.system.executionContext

      msg match
        case Register(id, name, imageUrl, price, description, replyTo) =>
          val usecase = RegisterProductUseCaseImpl(
            ProductIdGenerator: () =>
              ProductId(id),
            productRepository
          )
          usecase
            .execute(name, imageUrl, price, description): event =>
              replyTo ! event
          Behaviors.same

        case Edit(id, replyTo, nameOpt, imageUrlOpt, priceOpt, descriptionOpt) =>
          val usecase = EditProductUseCaseImpl(productRepository)
          usecase
            .execute(id, nameOpt, imageUrlOpt, priceOpt, descriptionOpt): event =>
              replyTo ! event
          Behaviors.same

        case Store(product) =>
          val productRepository = ProductActorRepository(Some(product), ctx)
          active(productRepository)

enum Command:
  case Store(product: Product)
  case Register(
      id: String,
      name: String,
      imageUrl: String,
      price: Int,
      description: String,
      replyTo: ActorRef[ProductEvent]
  )
  case Edit(
      id: String,
      replyTo: ActorRef[ProductEvent],
      nameOpt: Option[String] = None,
      imageUrlOpt: Option[String] = None,
      priceOpt: Option[Int] = None,
      descriptionOpt: Option[String] = None
  )
