package com.kuramapommel.til_akka_typed.domain

import scala.concurrent.{ExecutionContext, Future}
import cats.data.EitherT
import io.github.iltotore.iron.*

package object model:
  import event._
  import error._

  /** 商品ID.
    * @param value
    *   商品IDの値
    */
  case class ProductId(value: String)

  final class IsURLFormat
  given Constraint[String, IsURLFormat] with
    /** URLの形式かどうかを判定する
      *
      * @param value
      *   判定対象の文字列
      * @return
      *   URLの形式かどうか "https?://[\w/:%#\$&\?\(\)~\.=\+\-]+" で判定
      */
    // override inline def test(value: String): Boolean = value.matches("""https?://[\w/:%#\$&\?\(\)~\.=\+\-]+""")
    // todo 本当は正規表現を適応したいけどうまく実装できないので、とりあえず Iron 導入できたということで一時的に true を返すようにしておく
    override inline def test(value: String): Boolean = true

    /** エラーメッセージ
      *
      * @return
      */
    override inline def message: String = "URLの形式ではありません"

  object valueobject:

    /** 商品画像URL. */
    opaque type ImageURL = String :| IsURLFormat
    object ImageURL extends RefinedTypeOps[String, IsURLFormat, ImageURL]

  /** 商品IDジェネレータ */
  trait ProductIdGenerator:

    /** 商品IDの生成.
      * @return
      *   商品ID
      */
    def generate(): ExecutionContext ?=> EitherT[Future, ProductError, ProductId]

  /** 商品IDジェネレータ */
  object ProductIdGenerator:
    /** 商品IDの生成.
      * @param generateImpl
      *   商品IDの生成処理
      * @return
      *   商品IDジェネレータ
      */
    def apply(generateImpl: () => ProductId): ProductIdGenerator =
      new ProductIdGenerator:
        def generate(): ExecutionContext ?=> EitherT[Future, ProductError, ProductId] =
          EitherT.rightT[Future, ProductError](generateImpl())

  object event:
    import com.kuramapommel.til_akka_typed.domain.model.valueobject.ImageURL

    /** 商品イベント. */
    enum ProductEvent:
      /** 商品登録.
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

      /** 商品編集.
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
