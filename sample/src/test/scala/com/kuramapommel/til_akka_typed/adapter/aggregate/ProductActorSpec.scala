package com.kuramapommel.til_akka_typed.adapter.aggregate

import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.must.Matchers
import akka.actor.testkit.typed.scaladsl.ActorTestKit
import com.kuramapommel.til_akka_typed.domain.model.ProductId
import com.kuramapommel.til_akka_typed.domain.model.event.{
  ProductEvent,
  Registered
}

class ProductActorSpec
    extends AnyWordSpecLike
    with BeforeAndAfterAll
    with Matchers {
  val testKit = ActorTestKit()
  override def afterAll(): Unit = testKit.shutdownTestKit()

  "ProductActor" should {
    "プロダクトの登録が成功したとき Registered イベントが発生する" in {
      val productId = "test-id"
      val actor = testKit.spawn(ProductActor())
      val probe = testKit.createTestProbe[ProductEvent]()

      actor ! Command.Register(
        productId,
        "product1",
        "https://placehold.jp/123456/abcdef/150x150.png",
        100,
        "description",
        probe.ref
      )

      probe.expectMessage(
        Registered(
          ProductId(productId),
          "product1",
          "https://placehold.jp/123456/abcdef/150x150.png",
          100,
          "description"
        )
      )
    }
  }
}
