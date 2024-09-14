package com.kuramapommel.til_akka_typed.adapter.aggregate

import scala.concurrent.Promise
import scala.util.{Failure, Success}
import akka.actor.typed.{ActorRef, Behavior, DispatcherSelector}
import akka.actor.typed.scaladsl.Behaviors
import com.kuramapommel.til_akka_typed.domain.model.{Product, ProductId, ProductIdGenerator, ProductRepository}
import com.kuramapommel.til_akka_typed.domain.model.event.ProductEvent
import com.kuramapommel.til_akka_typed.usecase.RegisterProductUseCaseImpl

object ProductActor {
  def apply(): Behavior[Command] = active(None)
  def active(productOpt: Option[Product]): Behavior[Command] =
    Behaviors.receive[Command] { (ctx, msg) =>
      implicit val executionContext =
        ctx.system.executionContext

      msg match {
        case Command.Register(
               id,
               name,
               imageUrl,
               price,
               description,
               replyTo
             ) =>
          val promise = Promise[Product]()
          val productIdGenerator = ProductIdGenerator(() => ProductId(id))
          val productRepository = ProductRepository(
            id => productOpt.get,
            guest => {
              promise.success(guest)
              guest.id
            }
          )
          val usecase =
            new RegisterProductUseCaseImpl(
              productIdGenerator,
              productRepository
            )

          ctx.pipeToSelf(
            usecase
              .execute(name, imageUrl, price, description) { event =>
                replyTo ! event
              }
              .value
              .flatMap(_ => promise.future)
          ) {
            case Success(product)   => Command.Save(product)
            case Failure(exception) => ???
          }

          Behaviors.same
        case Command.Save(product) => active(Some(product))
      }
    }
}

sealed trait Command
object Command {
  case class Save(product: Product) extends Command
  case class Register(
      id: String,
      name: String,
      imageUrl: String,
      price: Int,
      description: String,
      replyTo: ActorRef[ProductEvent]
  ) extends Command
}
