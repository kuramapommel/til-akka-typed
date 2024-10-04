package com.kuramapommel.til_akka_typed.adapter.aggregate

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.Behaviors
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.Effect
import akka.persistence.typed.scaladsl.EventSourcedBehavior
import com.kuramapommel.til_akka_typed.domain.model.Product
import com.kuramapommel.til_akka_typed.domain.model.ProductIdGenerator
import com.kuramapommel.til_akka_typed.domain.model.event.ProductEvent
import com.kuramapommel.til_akka_typed.domain.model.valueobject.*
import com.kuramapommel.til_akka_typed.usecase.EditProductUseCaseImpl
import com.kuramapommel.til_akka_typed.usecase.RegisterProductUseCaseImpl
import scala.concurrent.ExecutionContext
import scala.concurrent.Promise

/** 商品アクター. */
object ProductActor:
  /**
   * アクター生成
   *
   * @param createPersistenceId
   *   Persistence Id 生成
   */
  def apply(createPersistenceId: () => PersistenceId): Behavior[Command] =
    import Command.*
    Behaviors.setup[Command]: context =>
      given ctx: ActorContext[Command] = context
      given ec: ExecutionContext = ctx.system.executionContext

      val persitenceId = createPersistenceId()
      val commandHandler: (Option[Product], Command) => Effect[ProductEvent, Option[Product]] =
        (productMaybe, command) =>
          val repository = ProductActorRepository(productMaybe)

          command match
            case Register(name, imageUrl, price, description, replyTo) =>
              val usecase =
                RegisterProductUseCaseImpl(
                  ProductIdGenerator: () =>
                    ProductId(persitenceId.id),
                  repository
                )

              val promise = Promise[ProductEvent]
              val result = for
                _ <- (usecase
                  .execute(name, imageUrl, price, description): event =>
                    promise.success(event))
                  .value
                event <- promise.future
              yield Effect
                .persist[ProductEvent, Option[Product]](event)
                .thenReply(replyTo): _ =>
                  event
              Effect
                .asyncReply(result)

            case Edit(id, replyTo, nameOpt, imageUrlOpt, priceOpt, descriptionOpt) =>
              val usecase = EditProductUseCaseImpl(repository)
              val promise = Promise[ProductEvent]
              val result = for
                _ <- (usecase
                  .execute(id, nameOpt, imageUrlOpt, priceOpt, descriptionOpt): event =>
                    promise.success(event))
                  .value
                event <- promise.future
              yield Effect
                .persist[ProductEvent, Option[Product]](event)
                .thenReply(replyTo): _ =>
                  event
              Effect
                .asyncReply(result)

      val eventHandler: (Option[Product], ProductEvent) => Option[Product] =
        case (None, ProductEvent.Registered(productId, name, imageUrl, price, description)) =>
          Some(Product(productId, name, imageUrl, price, description))

        case (Some(product), ProductEvent.Edited(productId, name, imageUrl, price, description)) =>
          Some(
            product.copy(
              name = name.getOrElse(product.name),
              imageUrl = imageUrl.getOrElse(product.imageUrl),
              price = price.getOrElse(product.price),
              description = description.getOrElse(product.description)
            )
          )
        case _ => ???

      EventSourcedBehavior[Command, ProductEvent, Option[Product]](
        persistenceId = persitenceId,
        emptyState = None,
        commandHandler = commandHandler,
        eventHandler = eventHandler
      )

/** 商品アクターコマンド */
enum Command:

  /**
   * 登録.
   * @param name
   *   商品名
   * @param imageUrl
   *   商品画像URL
   * @param price
   *   価格
   * @param description
   *   商品説明
   * @param replyTo
   *   返信先
   */
  case Register(
      name: String,
      imageUrl: ImageURL,
      price: Int,
      description: String,
      replyTo: ActorRef[ProductEvent]
  )

  /**
   * 編集.
   * @param id
   *   商品ID
   * @param replyTo
   *   返信先
   * @param nameOpt
   *   商品名
   * @param imageUrlOpt
   *   商品画像URL
   * @param priceOpt
   *   価格
   * @param descriptionOpt
   *   商品説明
   */
  case Edit(
      id: String,
      replyTo: ActorRef[ProductEvent],
      nameOpt: Option[String] = None,
      imageUrlOpt: Option[ImageURL] = None,
      priceOpt: Option[Int] = None,
      descriptionOpt: Option[String] = None
  )
