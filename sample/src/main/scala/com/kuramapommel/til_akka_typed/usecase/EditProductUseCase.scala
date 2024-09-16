package com.kuramapommel.til_akka_typed.usecase

import com.kuramapommel.til_akka_typed.domain.model.event.ProductEvent
import scala.concurrent.ExecutionContext
import cats.data.EitherT
import scala.concurrent.Future
import com.kuramapommel.til_akka_typed.domain.model.error.ProductError
import com.kuramapommel.til_akka_typed.domain.model.ProductId

trait EditProductUseCase:

  def execute(
      id: String,
      nameOpt: Option[String],
      imageUrlOpt: Option[String],
      priceOpt: Option[Int],
      descriptionOpt: Option[String]
  )(
      eventPublisher: ProductEvent => Unit
  )(implicit ec: ExecutionContext): EitherT[Future, ProductError, Unit]
