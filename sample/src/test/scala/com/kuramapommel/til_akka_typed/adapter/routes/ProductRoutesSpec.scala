package com.kuramapommel.til_akka_typed.adapter.routes

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport.*
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.*
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.kuramapommel.til_akka_typed.adapter.aggregate.ProductActor
import com.kuramapommel.til_akka_typed.adapter.routes.ProductRoutes.ProductCreateRequest
import com.kuramapommel.til_akka_typed.domain.model.ProductIdGenerator
import com.kuramapommel.til_akka_typed.domain.model.valueobject.*
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ProductRoutesSpec extends AnyWordSpec with Matchers with ScalaFutures with ScalatestRouteTest:
  lazy val testKit = ActorTestKit()
  given typedSystem: ActorSystem[?] = testKit.system

  "ProductRoutes" should:
    "商品を追加することができる (POST /product)" in:
      val productId = "1"
      val idGenerator = ProductIdGenerator: () =>
        ProductId(productId)
      val productActor = testKit.spawn(ProductActor(idGenerator))
      val routes = ProductRoutes(productActor).routes
      val productCreateRequest = ProductCreateRequest(
        name = "test",
        imageUrl = "https://placehold.jp/111111/777777/150x150.png",
        price = 100,
        description = "test"
      )
      val productCreateRequestEntity = Marshal(productCreateRequest).to[MessageEntity].futureValue

      val request = Post("/product").withEntity(productCreateRequestEntity)
      request ~> routes ~> check:
        status must be(StatusCodes.Created)
        contentType must be(ContentTypes.`application/json`)
        entityAs[String] must be("""{"productId":"1"}""")

    "商品画像URLがURLのフォーマットとして正しくない場合、BadRequest が返る (POST /product)" in:
      val productId = "1"
      val idGenerator = ProductIdGenerator: () =>
        ProductId(productId)
      val productActor = testKit.spawn(ProductActor(idGenerator))
      val routes = ProductRoutes(productActor).routes
      val productCreateRequest = ProductCreateRequest(
        name = "test",
        imageUrl = "bad-request",
        price = 100,
        description = "test"
      )
      val productCreateRequestEntity = Marshal(productCreateRequest).to[MessageEntity].futureValue

      val request = Post("/product").withEntity(productCreateRequestEntity)
      request ~> routes ~> check:
        status must be(StatusCodes.BadRequest)
        contentType must be(ContentTypes.`application/json`)
        entityAs[String] must be("""{"message":"URL形式で指定してください"}""")
