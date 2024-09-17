package com.kuramapommel.til_akka_typed.domain.model

import scala.concurrent.{ExecutionContext, Future}
import cats.data.EitherT
import com.kuramapommel.til_akka_typed.domain.model.error.ProductError

trait ProductRepository:
  def findById(id: ProductId): ExecutionContext ?=> EitherT[Future, ProductError, Product]
  def save(product: Product): ExecutionContext ?=> EitherT[Future, ProductError, ProductId]
