package com.kuramapommel.til_akka_typed.adapter.aggregate

import scala.concurrent.Promise
import scala.util.{Failure, Success}
import akka.actor.typed.{ActorRef, Behavior, DispatcherSelector}
import akka.actor.typed.scaladsl.Behaviors
import com.kuramapommel.til_akka_typed.domain.model.{Product, ProductId, ProductIdGenerator, ProductRepository}
import com.kuramapommel.til_akka_typed.domain.model.event.ProductEvent
import com.kuramapommel.til_akka_typed.usecase.RegisterProductUseCaseImpl
import com.kuramapommel.til_akka_typed.usecase.EditProductUseCase
import com.kuramapommel.til_akka_typed.usecase.EditProductUseCaseImpl

object ProductActor:
  def apply(): Behavior[Command] =
    active(None)

  def active(productOpt: Option[Product]): Behavior[Command] =
    Behaviors.receive[Command]: (ctx, msg) =>
      implicit val executionContext =
        ctx.system.executionContext
      msg match
        case Command.Register(id, name, imageUrl, price, description, replyTo) =>
          val promise = Promise[Product]
          val productIdGenerator = ProductIdGenerator(() => ProductId(id))
          val productRepository = ProductRepository(
            id => productOpt.get,
            guest =>
              promise.success(guest)
              guest.id
          )
          val usecase =
            new RegisterProductUseCaseImpl(productIdGenerator, productRepository)

          ctx.pipeToSelf(
            usecase
              .execute(name, imageUrl, price, description): event =>
                replyTo ! event
              .value
              .flatMap: ? =>
                promise.future
          ):
            case Success(product)   => Command.Save(product)
            case Failure(exception) => ???
          Behaviors.same

        case Command.Edit(id, replyTo, nameOpt, imageUrlOpt, priceOpt, descriptionOpt) =>
          val promise = Promise[Product]
          val productRepository = ProductRepository(
            id => productOpt.get,
            guest =>
              promise.success(guest)
              guest.id
          )
          val usecase = EditProductUseCaseImpl(productRepository)

          ctx.pipeToSelf(
            usecase
              .execute(id, nameOpt, imageUrlOpt, priceOpt, descriptionOpt): event =>
                replyTo ! event
              .value
              .flatMap: ? =>
                promise.future
          ):
            case Success(_)         => Command.Save(productOpt.get)
            case Failure(exception) => ???
          Behaviors.same

        case Command.Save(product) => active(Some(product))

enum Command:
  case Save(product: Product)
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