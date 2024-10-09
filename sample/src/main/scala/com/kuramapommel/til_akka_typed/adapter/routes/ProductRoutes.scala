package com.kuramapommel.til_akka_typed.adapter.routes

import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.AskPattern.*
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport.*
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.*
import akka.util.Timeout
import com.kuramapommel.til_akka_typed.adapter.aggregate.Command
import com.kuramapommel.til_akka_typed.domain.model.event.ProductEvent
import com.kuramapommel.til_akka_typed.domain.model.valueobject.*
import io.github.iltotore.iron.*
import spray.json.DefaultJsonProtocol.*
import spray.json.RootJsonFormat

/** 商品 API. */
object ProductRoutes:

  /**
   * 商品作成 API リクエスト.
   *
   * @param name
   *    商品名
   * @param imageUrl
   *   商品画像URL
   * @param price
   *   価格
   * @param description
   *   商品説明
   */
  case class ProductCreateRequest(
      name: String,
      imageUrl: String,
      price: Int,
      description: String
  )

  /** 商品生成 API リクエストデコーダ */
  given productCreateJsonFormat: RootJsonFormat[ProductCreateRequest] = jsonFormat4(ProductCreateRequest.apply)

  /**
   * 商品作成 API レスポンス.
   *
   * @param productId
   *   商品ID
   */
  case class ProductCreatedResponse(productId: String)

  /** 商品生成 API レスポンスエンコーダ */
  given productCreatedJsonFormat: RootJsonFormat[ProductCreatedResponse] = jsonFormat1(ProductCreatedResponse.apply)

  /**
   * 商品情報編集 API リクエスト.
   *
   * @param name
   *   商品名
   * @param imageUrl
   *   商品画像URL
   * @param price
   *   価格
   * @param description
   *   商品説明
   */
  case class ProductEditRequest(
      name: Option[String] = None,
      imageUrl: Option[String] = None,
      price: Option[Int] = None,
      description: Option[String] = None
  )

  /** 商品情報編集 API リクエストデコーダ */
  given productEditJsonFormat: RootJsonFormat[ProductEditRequest] = jsonFormat4(ProductEditRequest.apply)

  /**
   * 商品情報編集 API レスポンス.
   *
   * @param productId
   *  商品ID
   * @param name
   *  商品名
   * @param imageUrl
   *  商品画像URL
   * @param price
   *  価格
   * @param description
   *  商品説明
   */
  case class ProductEditedResponse(
      productId: String,
      name: Option[String] = None,
      imageUrl: Option[String] = None,
      price: Option[Int] = None,
      description: Option[String] = None
  )

  /** 商品情報編集 API レスポンスエンコーダ */
  given productEditedJsonFormat: RootJsonFormat[ProductEditedResponse] = jsonFormat5(ProductEditedResponse.apply)

  /**
   * API エラーレスポンス
   *
   * @param message
   *   エラーメッセージ
   */
  case class ErrorMessageResponse(message: String)

  /** API エラー レスポンスエンコーダ */
  given errorMessageJsonFormat: RootJsonFormat[ErrorMessageResponse] = jsonFormat1(ErrorMessageResponse.apply)

/**
 * 商品作成 API リクエスト.
 *
 * @param productActor 商品アクター
 * @param system アクターシステム
 */
class ProductRoutes(productActor: ActorRef[Command])(using system: ActorSystem[?]):
  import ProductRoutes.*

  given timeout: Timeout =
    Timeout.create(system.settings.config.getDuration("my-app.routes.ask-timeout"))

  val routes = pathPrefix("product"):
    concat(
      pathEnd:
        post:
          entity(as[ProductCreateRequest]): product =>
            val commandMaybe =
              for imageUrl <- product.imageUrl.refineEither[URLBase]
              yield Command.Register(product.name, imageUrl, product.price, product.description, _)

            commandMaybe match
              case Right(command) =>
                onSuccess(
                  productActor.ask(command)
                ):
                  case ProductEvent.Registered(productId, _, _, _, _) =>
                    complete((StatusCodes.Created, ProductCreatedResponse(productId.value)))
                  case _ => complete((StatusCodes.InternalServerError, """{"message":"unkown error"}"""))
              case Left(message) =>
                complete((StatusCodes.BadRequest, ErrorMessageResponse(message)))
      ,
      path(Segment): productId =>
        pathEnd:
          post:
            entity(as[ProductEditRequest]): product =>
              val commandMaybe =
                for imageUrlMaybe: Option[IronType[String, URLBase]] <- (product.imageUrl.map: imageUrl =>
                    imageUrl.refineEither[URLBase]) match
                    case Some(Right(imageUrl)) => Right(Some(imageUrl))
                    case Some(Left(message))   => Left(message)
                    case None                  => Right(None)
                yield Command.Edit(
                  Some(productId),
                  _,
                  product.name,
                  imageUrlMaybe,
                  product.price,
                  product.description
                )

              commandMaybe match
                case Right(command) =>
                  onSuccess(
                    productActor.ask(command)
                  ):
                    case ProductEvent.Edited(productId, name, imageUrl, price, description) =>
                      complete(
                        (StatusCodes.OK, ProductEditedResponse(productId.value, name, imageUrl, price, description))
                      )
                    case _ => complete((StatusCodes.InternalServerError, """{"message":"unkown error"}"""))
                case Left(message) =>
                  complete((StatusCodes.BadRequest, ErrorMessageResponse(message)))
    )
