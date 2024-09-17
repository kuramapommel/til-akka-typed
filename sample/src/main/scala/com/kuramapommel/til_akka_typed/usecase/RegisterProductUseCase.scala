package com.kuramapommel.til_akka_typed.usecase

import com.kuramapommel.til_akka_typed.domain.model.event.ProductEvent
import scala.concurrent.ExecutionContext
import cats.data.EitherT
import scala.concurrent.Future
import com.kuramapommel.til_akka_typed.domain.model.error.ProductError

trait RegisterProductUseCase:

  def execute(name: String, imageUrl: String, price: Int, description: String)(
      eventPublisher: ProductEvent => Unit
  ): ExecutionContext ?=> EitherT[Future, ProductError, Unit]
