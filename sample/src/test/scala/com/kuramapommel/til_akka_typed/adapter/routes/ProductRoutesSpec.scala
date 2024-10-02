package com.kuramapommel.til_akka_typed.adapter.routes

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport.*
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.*
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.persistence.typed.PersistenceId
import com.kuramapommel.til_akka_typed.adapter.aggregate.ProductActor
import com.kuramapommel.til_akka_typed.adapter.routes.ProductRoutes.*
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ProductRoutesSpec extends AnyWordSpec with Matchers with ScalaFutures with ScalatestRouteTest:
  lazy val testKit = ActorTestKit()
  given typedSystem: ActorSystem[?] = testKit.system

  "ProductRoutes" should:
    "商品を追加することができる (POST /product)" in:
      val productId = "1"
      val productActor = testKit.spawn(ProductActor(() => PersistenceId.ofUniqueId(productId)))
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
        entityAs[String] must be(s"""{"productId":"$productId"}""")

    "商品画像URLがURLのフォーマットとして正しくない場合、BadRequest が返る (POST /product)" in:
      val productId = "1"
      val productActor = testKit.spawn(ProductActor(() => PersistenceId.ofUniqueId(productId)))
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

    "商品情報を編集することができる (POST /product/{productId})" in:
      val productId = "1"
      val productActor = testKit.spawn(ProductActor(() => PersistenceId.ofUniqueId(productId)))
      val routes = ProductRoutes(productActor).routes
      val productCreateRequestEntity = Marshal(
        ProductCreateRequest(
          name = "test",
          imageUrl = "https://placehold.jp/111111/777777/150x150.png",
          price = 100,
          description = "test"
        )
      ).to[MessageEntity].futureValue
      val request = Post("/product").withEntity(productCreateRequestEntity)
      request ~> routes ~> check:
        val name = "test-product"
        val imageUrl = "https://placehold.jp/777777/111111/150x150.png"
        val price = 200
        val description = "test-description"
        val productEditRequest = ProductEditRequest(
          name = Some(name),
          imageUrl = Some(imageUrl),
          price = Some(price),
          description = Some(description)
        )
        val productEditRequestEntity = Marshal(productEditRequest).to[MessageEntity].futureValue

        val request = Post(s"/product/$productId").withEntity(productEditRequestEntity)
        request ~> routes ~> check:
          status must be(StatusCodes.OK)
          contentType must be(ContentTypes.`application/json`)
          entityAs[String] must be(
            s"""{"description":"$description","imageUrl":"$imageUrl","name":"$name","price":$price,"productId":"$productId"}"""
          )

    "商品情報の一部を編集した場合、レスポンスに編集していないプロパティは含まれない (POST /product/{productId})" in:
      val productId = "1"
      val productActor = testKit.spawn(ProductActor(() => PersistenceId.ofUniqueId(productId)))
      val routes = ProductRoutes(productActor).routes
      val productCreateRequestEntity = Marshal(
        ProductCreateRequest(
          name = "test",
          imageUrl = "https://placehold.jp/111111/777777/150x150.png",
          price = 100,
          description = "test"
        )
      ).to[MessageEntity].futureValue
      val request = Post("/product").withEntity(productCreateRequestEntity)
      request ~> routes ~> check:
        val name = "test-product"
        val price = 200
        val productEditRequest = ProductEditRequest(
          name = Some(name),
          price = Some(price)
        )
        val productEditRequestEntity = Marshal(productEditRequest).to[MessageEntity].futureValue

        val request = Post(s"/product/$productId").withEntity(productEditRequestEntity)
        request ~> routes ~> check:
          status must be(StatusCodes.OK)
          contentType must be(ContentTypes.`application/json`)
          entityAs[String] must be(
            s"""{"name":"$name","price":$price,"productId":"$productId"}"""
          )
