package com.kuramapommel.til_akka_typed.usecase

import cats.data.EitherT
import com.kuramapommel.til_akka_typed.domain.model.Product
import com.kuramapommel.til_akka_typed.domain.model.ProductRepository
import com.kuramapommel.til_akka_typed.domain.model.error.ProductError
import com.kuramapommel.til_akka_typed.domain.model.event.ProductEvent
import com.kuramapommel.til_akka_typed.domain.model.valueobject.*
import io.github.iltotore.iron.*
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.Promise

class EditProductUseCaseImplSpec extends ScalaFutures with Matchers with AnyWordSpecLike:
  import scala.concurrent.ExecutionContext.Implicits.global

  "EditProductUseCaseImpl" should:
    "プロダクトの編集が成功したとき Edited イベントが発生する" in:
      val productId = "test-id"
      val repository = new ProductRepository:
        def findById(id: ProductId): ExecutionContext ?=> EitherT[Future, ProductError, Product] =
          EitherT.rightT[Future, ProductError](
            Product(id, "product1", "https://placehold.jp/123456/abcdef/150x150.png", 100, "description")
          )

        def save(product: Product): ExecutionContext ?=> EitherT[Future, ProductError, ProductId] =
          EitherT.rightT[Future, ProductError](product.id)

      val name = "商品"
      val imageUrl: ImageURL = "https://placehold.jp/abcdef/123456/150x150.png"
      val price = 200
      val description = "説明"
      val usecase = new EditProductUseCaseImpl(repository)

      val promise = Promise[ProductEvent]()
      val result = usecase.execute(productId, Some(name), Some(imageUrl), Some(price), Some(description)): event =>
        promise.success(event)

      whenReady(result.value):
        case Right(_) =>
          whenReady(promise.future):
            case ProductEvent.Edited(
                   actualProductId,
                   Some(actualName),
                   Some(actualImageUrl),
                   Some(actualPrice),
                   Some(actualDescription)
                 ) =>
              (actualProductId, actualName, actualImageUrl, actualPrice, actualDescription) must be(
                (ProductId(productId), name, imageUrl, price, description)
              )
            case _ => fail()
        case Left(_) => fail()
