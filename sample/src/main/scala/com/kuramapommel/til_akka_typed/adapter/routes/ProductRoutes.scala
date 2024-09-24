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
import io.github.iltotore.iron.cats.*
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
   * API エラーレスポンス
   *
   * @param message
   *   エラーメッセージ
   */
  case class ErrorMessageResponse(message: String)

  /** 商品生成 API レスポンスエンコーダ */
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
              ): registered =>
                registered match
                  case ProductEvent.Registered(productId, _, _, _, _) =>
                    complete((StatusCodes.Created, ProductCreatedResponse(productId.value)))
                  case _ => complete((StatusCodes.InternalServerError, """{"message":"unkown error"}"""))
            case Left(message) =>
              complete((StatusCodes.BadRequest, ErrorMessageResponse(message)))
