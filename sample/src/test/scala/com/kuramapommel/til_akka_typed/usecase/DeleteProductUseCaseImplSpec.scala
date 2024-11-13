package com.kuramapommel.til_akka_typed.usecase

import cats.data.EitherT
import com.kuramapommel.til_akka_typed.domain.model.Product
import com.kuramapommel.til_akka_typed.domain.model.ProductRepository
import com.kuramapommel.til_akka_typed.domain.model.error.ProductError
import com.kuramapommel.til_akka_typed.domain.model.event.ProductEvent
import com.kuramapommel.til_akka_typed.domain.model.valueobject.*
import io.github.iltotore.iron.*
import java.util.UUID
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.Promise

class DeleteProductUseCaseImplSpec extends ScalaFutures with Matchers with AnyWordSpecLike:
  import scala.concurrent.ExecutionContext.Implicits.global

  "DeleteProductUseCaseImpl" should:
    "プロダクトの削除が成功したとき Deleted イベントが発生する" in:
      val productId = UUID.randomUUID().toString()
      val repository = new ProductRepository:
        def findById(id: ProductId): ExecutionContext ?=> EitherT[Future, ProductError, Product] =
          EitherT.rightT[Future, ProductError](
            Product(id, "product1", "https://placehold.jp/123456/abcdef/150x150.png", 100, "description")
          )

        def save(product: Product): ExecutionContext ?=> EitherT[Future, ProductError, ProductId] =
          EitherT.rightT[Future, ProductError](product.id)

      val usecase = new DeleteProductUseCaseImpl(repository)

      val promise = Promise[ProductEvent]()
      val result = usecase.execute(productId): event =>
        promise.success(event)

      whenReady(for
        _ <- result.value
        event <- promise.future
      yield event):
        case ProductEvent.Deleted(
               actualProductId,
               actualDeleted
             ) =>
          (actualProductId, actualDeleted) must be(
            (ProductId(productId), true)
          )
        case _ => fail()
