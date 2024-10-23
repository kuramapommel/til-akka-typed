package com.kuramapommel.til_akka_typed.adapter.aggregate

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.cluster.sharding.typed.scaladsl.EntityTypeKey
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.SnapshotSelectionCriteria
import akka.persistence.typed.scaladsl.Effect
import akka.persistence.typed.scaladsl.EventSourcedBehavior
import cats.data.EitherT
import com.kuramapommel.til_akka_typed.domain.model.Product
import com.kuramapommel.til_akka_typed.domain.model.ProductIdGenerator
import com.kuramapommel.til_akka_typed.domain.model.error.ProductError
import com.kuramapommel.til_akka_typed.domain.model.event.ProductEvent
import com.kuramapommel.til_akka_typed.domain.model.valueobject.*
import com.kuramapommel.til_akka_typed.usecase.EditProductUseCaseImpl
import com.kuramapommel.til_akka_typed.usecase.RegisterProductUseCaseImpl
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.Promise

/** 商品アクター. */
object ProductActor:

  /** クラスタ全体での一意性を担保するキー */
  val typeKey: EntityTypeKey[Command] = EntityTypeKey[Command]("ProductActor")

  /**
   * アクター生成
   *
   * @param createPersistenceId
   *   Persistence Id 生成
   */
  def apply(createPersistenceId: () => PersistenceId): ExecutionContext ?=> Behavior[Command] =
    import Command.*
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

    val persitenceId = createPersistenceId()
    val idGenerator = ProductIdGenerator: () =>
      ProductId(persitenceId.entityId)

    val commandHandler: (Option[Product], Command) => Effect[ProductEvent, Option[Product]] =
      (productMaybe, command) =>
        val repository = ProductActorRepository(productMaybe)
        val promise = Promise[ProductEvent]
        val (usecase, replyTo) = command match
          case Register(name, imageUrl, price, description, replyTo) =>
            RegisterProductUseCaseImpl(idGenerator, repository)
              .execute(name, imageUrl, price, description)
              .tuple(replyTo)
          case Edit(Some(id), replyTo, nameOpt, imageUrlOpt, priceOpt, descriptionOpt) =>
            EditProductUseCaseImpl(repository)
              .execute(id, nameOpt, imageUrlOpt, priceOpt, descriptionOpt)
              .tuple(replyTo)
          case _ => ???

        val result = for
          result <- usecase(promise.success).value
          event <- result match
            case Right(_)    => promise.future
            case Left(error) => ???
        yield Effect
          .persist[ProductEvent, Option[Product]](event)
          .thenReply(replyTo): _ =>
            event
        Effect
          .asyncReply(result)

    EventSourcedBehavior[Command, ProductEvent, Option[Product]](
      persistenceId = persitenceId,
      emptyState = None,
      commandHandler = commandHandler,
      eventHandler = eventHandler
    ).snapshotWhen:
      case (_, _, _) => true
    .withSnapshotSelectionCriteria(
        SnapshotSelectionCriteria.latest // 最新のスナップショットから復元
      )

  /** カリー化されたユースケース実行の拡張 */
  extension (curriedExecution: (ProductEvent => Unit) => EitherT[Future, ProductError, Unit])
    /**
     * タプル化
     *
     * @param replyTo
     *  タプル化するアクター
     */
    def tuple(
        replyTo: ActorRef[ProductEvent]
    ): ((ProductEvent => Unit) => EitherT[Future, ProductError, Unit], ActorRef[ProductEvent]) =
      (curriedExecution, replyTo)

/** 商品アクターコマンド */
enum Command:

  val id: Option[String] = None

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
      override val id: Option[String],
      replyTo: ActorRef[ProductEvent],
      nameOpt: Option[String] = None,
      imageUrlOpt: Option[ImageURL] = None,
      priceOpt: Option[Int] = None,
      descriptionOpt: Option[String] = None
  )
