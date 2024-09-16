package com.kuramapommel.til_akka_typed.domain

import scala.concurrent.{ExecutionContext, Future}
import cats.data.EitherT

package object model:
  import event._
  import error._
  case class ProductId(value: String)

  trait ProductIdGenerator:
    def generate()(implicit ec: ExecutionContext): EitherT[Future, ProductError, ProductId]

  object ProductIdGenerator:
    def apply(generateImpl: () => ProductId): ProductIdGenerator =
      new ProductIdGenerator:
        def generate()(implicit ec: ExecutionContext): EitherT[Future, ProductError, ProductId] =
          EitherT.rightT[Future, ProductError](generateImpl())

  object event:
    enum ProductEvent:
      case Registered(productId: ProductId, name: String, imageUrl: String, price: Int, description: String)
      case Edited(
          productId: ProductId,
          name: Option[String] = None,
          imageUrl: Option[String] = None,
          price: Option[Int] = None,
          description: Option[String] = None
      )

  object error:
    enum ProductError:
      case NotFound
