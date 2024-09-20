package com.kuramapommel.til_akka_typed.usecase

import scala.concurrent.{ExecutionContext, Future, Promise}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import cats.data.EitherT
import io.github.iltotore.iron.*
import com.kuramapommel.til_akka_typed.domain.model.{Product, ProductIdGenerator, ProductRepository}
import com.kuramapommel.til_akka_typed.domain.model.valueobject._
import com.kuramapommel.til_akka_typed.domain.model.event.ProductEvent
import com.kuramapommel.til_akka_typed.domain.model.error.ProductError

class RegisterProductUseCaseImplSpec extends ScalaFutures with Matchers with AnyWordSpecLike:
  import scala.concurrent.ExecutionContext.Implicits.global

  "RegisterProductUseCaseImpl" should:
    "プロダクトの登録が成功したとき Registered イベントが発生する" in:
      val productId = ProductId("test-id")
      val name = "product1"
      val imageUrl: ImageURL = "https://placehold.jp/123456/abcdef/150x150.png"
      val price = 100
      val description = "description"

      var savedId = ProductId("error")
      val repository = new ProductRepository:
        def findById(id: ProductId): ExecutionContext ?=> EitherT[Future, ProductError, Product] =
          // findById は呼ばれるはずがないので、失敗させる
          fail()

        def save(product: Product): ExecutionContext ?=> EitherT[Future, ProductError, ProductId] =
          // save は呼ばれるはずなので、保存した ID を一時保存しておく
          savedId = product.id
          EitherT.rightT[Future, ProductError](savedId)
      val usecase = new RegisterProductUseCaseImpl(
        ProductIdGenerator: () =>
          productId,
        repository
      )

      val promise = Promise[ProductEvent]()
      val result = usecase.execute(name, imageUrl, price, description): event =>
        promise.success(event)

      whenReady(result.value):
        case Right(_) =>
          whenReady(promise.future):
            case ProductEvent.Registered(actualProductId, actualName, actualImageUrl, actualPrice, actualDescription) =>
              (actualProductId, actualName, actualImageUrl, actualPrice, actualDescription, savedId) must be(
                (productId, name, imageUrl, price, description, productId)
              )
            case _ => fail()
        case Left(_) => fail()
