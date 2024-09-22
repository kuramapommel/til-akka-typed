package com.kuramapommel.til_akka_typed.adapter.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import spray.json.DefaultJsonProtocol.*
import spray.json.RootJsonFormat

object ProductRoutes:
  val routes: Route = pathPrefix("product"):
    pathEnd:
      post:
        complete((StatusCodes.Created, """{"productId":"1"}"""))

  case class ProductCreateRequest(
      name: String,
      imageUrl: String,
      price: Int,
      description: String
  )
  given productCreateJsonFormat: RootJsonFormat[ProductCreateRequest] = jsonFormat4(ProductCreateRequest.apply)
