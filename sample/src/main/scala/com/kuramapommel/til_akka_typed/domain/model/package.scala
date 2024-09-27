package com.kuramapommel.til_akka_typed.domain

import cats.data.EitherT
import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.string.Match
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

package object model:
  import error.*
  import valueobject.*

  /** 商品IDジェネレータ */
  trait ProductIdGenerator:

    /**
     * 商品IDの生成.
     * @return
     *   商品ID
     */
    def generate(): ExecutionContext ?=> EitherT[Future, ProductError, ProductId]

  /** 商品IDジェネレータ */
  object ProductIdGenerator:
    /**
     * 商品IDの生成.
     * @param generateImpl
     *   商品IDの生成処理
     * @return
     *   商品IDジェネレータ
     */
    def apply(generateImpl: () => ProductId): ProductIdGenerator =
      new ProductIdGenerator:
        def generate(): ExecutionContext ?=> EitherT[Future, ProductError, ProductId] =
          EitherT.rightT[Future, ProductError](generateImpl())

  object valueobject:
    /**
     * 商品ID.
     * @param value
     *   商品IDの値
     */
    case class ProductId(value: String)

    /** URL. */
    type URLBase = DescribedAs[
      Match["""https?://[\w/:%#\$&\?\(\)~\.=\+\-]+"""],
      "URL形式で指定してください"
    ]

    /** 商品画像URL. */
    type ImageURL = String :| URLBase

  object event:

    /** 商品イベント. */
    enum ProductEvent:
      /**
       * 商品登録.
       * @param productId
       *   商品ID
       * @param name
       *   商品名
       * @param imageUrl
       *   商品画像URL
       * @param price
       *   価格
       * @param description
       *   商品説明
       */
      case Registered(productId: ProductId, name: String, imageUrl: ImageURL, price: Int, description: String)

      /**
       * 商品編集.
       * @param productId
       *   商品ID
       * @param name
       *   商品名
       * @param imageUrl
       *   商品画像URL
       * @param price
       *   価格
       * @param description
       *   商品説明
       */
      case Edited(
          productId: ProductId,
          name: Option[String] = None,
          imageUrl: Option[ImageURL] = None,
          price: Option[Int] = None,
          description: Option[String] = None
      )

  object error:
    /** 商品エラー. */
    enum ProductError:
      /** 商品未登録. */
      case NotFound
