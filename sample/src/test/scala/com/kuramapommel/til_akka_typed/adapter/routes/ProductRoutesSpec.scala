package com.kuramapommel.til_akka_typed.adapter.routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport.*
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.*
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.kuramapommel.til_akka_typed.adapter.routes.ProductRoutes.ProductCreateRequest
import com.kuramapommel.til_akka_typed.adapter.routes.ProductRoutes.given
import com.kuramapommel.til_akka_typed.adapter.routes.ProductRoutes.routes
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ProductRoutesSpec extends AnyWordSpec with Matchers with ScalaFutures with ScalatestRouteTest:

  "ProductRoutes" should {
    "商品を追加することができる (POST /product)" in {
      val productCreateRequest = ProductCreateRequest(
        name = "test",
        imageUrl = "test",
        price = 100,
        description = "test"
      )
      val productCreateRequestEntity = Marshal(productCreateRequest).to[MessageEntity].futureValue

      val request = Post("/product").withEntity(productCreateRequestEntity)
      request ~> routes ~> check:
        status must be(StatusCodes.Created)
        entityAs[String] must be("""{"productId":"1"}""")
    }
  }
