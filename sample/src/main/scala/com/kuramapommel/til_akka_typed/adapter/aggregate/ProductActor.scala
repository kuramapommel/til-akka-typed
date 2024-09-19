package com.kuramapommel.til_akka_typed.adapter.aggregate

import scala.concurrent.Promise
import scala.util.{Failure, Success}
import akka.actor.typed.{ActorRef, Behavior, DispatcherSelector}
import akka.actor.typed.scaladsl.Behaviors
import com.kuramapommel.til_akka_typed.domain.model.{Product, ProductId, ProductIdGenerator, ProductRepository}
import com.kuramapommel.til_akka_typed.domain.model.valueobject.ImageURL
import com.kuramapommel.til_akka_typed.domain.model.event.ProductEvent
import com.kuramapommel.til_akka_typed.usecase.RegisterProductUseCaseImpl
import com.kuramapommel.til_akka_typed.usecase.EditProductUseCaseImpl
import scala.concurrent.ExecutionContext

/** 商品アクター. */
object ProductActor:
  /** 商品アクターの生成.
    * @return
    *   商品アクター
    */
  def apply(): Behavior[Command] =
    Behaviors.setup[Command]: (ctx) =>
      val productRepository = ProductActorRepository(None, ctx)
      active(productRepository)

  /** 商品アクターの動作.
    * @param productRepository
    *   商品リポジトリ
    * @return
    *   商品アクター
    */
  def active(productRepository: ProductRepository): Behavior[Command] =
    Behaviors.receive[Command]: (ctx, msg) =>
      import Command._
      given executionContext: ExecutionContext =
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

/** 商品アクターコマンド */
enum Command:
  /** 保存.
    * @param product
    *   商品
    */
  case Store(product: Product)

  /** 登録.
    * @param id
    *   商品ID
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
      id: String,
      name: String,
      imageUrl: ImageURL,
      price: Int,
      description: String,
      replyTo: ActorRef[ProductEvent]
  )

  /** 編集.
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
