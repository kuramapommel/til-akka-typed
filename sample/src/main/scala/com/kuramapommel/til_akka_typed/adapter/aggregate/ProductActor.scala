package com.kuramapommel.til_akka_typed.adapter.aggregate

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.Behaviors
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.Effect
import akka.persistence.typed.scaladsl.EventSourcedBehavior
import cats.data.EitherT
import com.kuramapommel.til_akka_typed.domain.model.Product
import com.kuramapommel.til_akka_typed.domain.model.ProductIdGenerator
import com.kuramapommel.til_akka_typed.domain.model.ProductRepository
import com.kuramapommel.til_akka_typed.domain.model.error.ProductError
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
   * 商品アクターの生成.
   * @return
   *   商品アクター
   */
  def apply(idGenerator: ProductIdGenerator): Behavior[Command] =
    Behaviors.setup[Command]: (ctx) =>
      val productRepository = ProductActorRepository(None, ctx)
      active(productRepository, idGenerator)

  /**
   * 商品アクターの動作.
   * @param productRepository
   *   商品リポジトリ
   * @return
   *   商品アクター
   */
  def active(productRepository: ProductRepository, idGenerator: ProductIdGenerator): Behavior[Command] =
    Behaviors.receive[Command]: (ctx, msg) =>
      import Command.*
      given executionContext: ExecutionContext =
        ctx.system.executionContext

      msg match
        case Register(name, imageUrl, price, description, replyTo) =>
          val usecase = RegisterProductUseCaseImpl(
            idGenerator,
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
          active(productRepository, idGenerator)

        case Persist(event) =>
          Behaviors.same

  def apply(productId: ProductId)(using ctx: ActorContext[Command]): Behavior[Command] =
    val commandHandler: (Option[Product], Command) => Effect[ProductEvent, Option[Product]] =
      (productMaybe, command) =>
        import Command.*
        given executionContext: ExecutionContext =
          ctx.system.executionContext
        val repository = new ProductRepository:
          override def findById(id: ProductId): ExecutionContext ?=> EitherT[Future, ProductError, Product] =
            productMaybe match
              case Some(product) if product.id == id =>
                EitherT.rightT[Future, ProductError](product)
              case _ =>
                EitherT.leftT[Future, Product](ProductError.NotFound)

          override def save(product: Product): ExecutionContext ?=> EitherT[Future, ProductError, ProductId] =
            EitherT.rightT[Future, ProductError](product.id)

        command match
          case Register(name, imageUrl, price, description, replyTo) =>
            val usecase = RegisterProductUseCaseImpl(ProductIdGenerator(() => productId), repository)
            usecase
              .execute(name, imageUrl, price, description): event =>
                ctx.pipeToSelf(Future.successful(event)):
                  case Success(event) => Command.Persist(event)
                  case _              => ???
            Effect.none

          case Edit(id, replyTo, nameOpt, imageUrlOpt, priceOpt, descriptionOpt) =>
            val usecase = EditProductUseCaseImpl(repository)
            usecase
              .execute(id, nameOpt, imageUrlOpt, priceOpt, descriptionOpt): event =>
                ctx.pipeToSelf(Future.successful(event)):
                  case Success(event) => Command.Persist(event)
                  case _              => ???
            Effect.none

          case Store(product) =>
            Effect.none

          case Persist(event) => Effect.persist(event)

    val eventHandler: (Option[Product], ProductEvent) => Option[Product] = (productMaybe, event) =>
      event match
        case ProductEvent.Registered(productId, name, imageUrl, price, description) =>
          productMaybe.orElse(Some(Product(productId, name, imageUrl, price, description)))

        case ProductEvent.Edited(productId, name, imageUrl, price, description) =>
          productMaybe.map((product) =>
            product.copy(
              name = name.getOrElse(product.name),
              imageUrl = imageUrl.getOrElse(product.imageUrl),
              price = price.getOrElse(product.price),
              description = description.getOrElse(product.description)
            )
          )

    EventSourcedBehavior[Command, ProductEvent, Option[Product]](
      persistenceId = PersistenceId.ofUniqueId(productId.value),
      emptyState = None,
      commandHandler = commandHandler,
      eventHandler = eventHandler
    )

/** 商品アクターコマンド */
enum Command:
  /**
   * 保存.
   * @param product
   *   商品
   */
  case Store(product: Product)

  case Persist(event: ProductEvent)

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
