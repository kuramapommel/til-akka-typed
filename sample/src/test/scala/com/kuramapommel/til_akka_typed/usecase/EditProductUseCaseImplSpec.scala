package com.kuramapommel.til_akka_typed.usecase

import com.kuramapommel.til_akka_typed.domain.model.ProductId
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import com.kuramapommel.til_akka_typed.domain.model.event.ProductEvent
import scala.concurrent.Promise
import com.kuramapommel.til_akka_typed.domain.model.{Product, ProductRepository}

class EditProductUseCaseImplSpec extends ScalaFutures with Matchers with AnyWordSpecLike:
  import scala.concurrent.ExecutionContext.Implicits.global

  "EditProductUseCaseImpl" should:
    "プロダクトの編集が成功したとき Edited イベントが発生する" in:
      val productId = ProductId("test-id")
      val usecase = new EditProductUseCaseImpl(
        ProductRepository(
          id => Product(id, "product1", "https://placehold.jp/123456/abcdef/150x150.png", 100, "description"),
          guest => guest.id
        )
      )
      val promise = Promise[ProductEvent]()
      val name = "商品"
      val imageUrl = "https://placehold.jp/abcdef/123456/150x150.png"
      val price = 200
      val description = "説明"
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
                (productId, name, imageUrl, price, description)
              )
            case _ => fail()
        case Left(_) => fail()
