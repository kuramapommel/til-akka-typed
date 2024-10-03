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
import scala.concurrent.Future
import scala.util.Success

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
              usecase
                .execute(name, imageUrl, price, description): event =>
                  event.pipeToSelf(replyTo)
              Effect.none

            case Edit(id, replyTo, nameOpt, imageUrlOpt, priceOpt, descriptionOpt) =>
              val usecase = EditProductUseCaseImpl(repository)
              usecase
                .execute(id, nameOpt, imageUrlOpt, priceOpt, descriptionOpt): event =>
                  event.pipeToSelf(replyTo)
              Effect.none

            case Persist(event, replyTo) =>
              Effect
                .persist(event)
                .thenReply(replyTo): _ =>
                  event

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

  /** プロダクトイベントの拡張定義 */
  extension (event: ProductEvent)
    /**
     * 自身にイベントを送信する
     *
     * @param replyTo
     *   返信先アクター
     * @param ctx
     *   コンテキスト
     */
    def pipeToSelf(replyTo: ActorRef[ProductEvent])(using ctx: ActorContext[Command]) =
      ctx.pipeToSelf(Future.successful(event)):
        case Success(event) => Command.Persist(event, replyTo)
        case _              => ???

/** 商品アクターコマンド */
enum Command:
  /**
   * 永続化
   * @param event
   *   商品集約イベント
   * @param replyTo
   *   返信先
   */
  case Persist(event: ProductEvent, replyTo: ActorRef[ProductEvent])

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
