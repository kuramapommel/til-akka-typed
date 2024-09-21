package com.kuramapommel.til_akka_typed.adapter.aggregate

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import com.kuramapommel.til_akka_typed.domain.model.event.ProductEvent
import com.kuramapommel.til_akka_typed.domain.model.valueobject.*
import io.github.iltotore.iron.*
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class ProductActorSpec extends AnyWordSpecLike with BeforeAndAfterAll with Matchers:
  val testKit = ActorTestKit()
  override def afterAll(): Unit =
    testKit.shutdownTestKit()

  "ProductActor" should:
    "Register コマンドを受信し処理が成功したとき Registered イベントが発生する" in:
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
        ProductEvent.Registered(
          ProductId(productId),
          "product1",
          "https://placehold.jp/123456/abcdef/150x150.png",
          100,
          "description"
        )
      )

    "Edit(productId, Some(\"商品\", sender)) コマンドを受信し処理が成功したとき Edited イベントが発生する" in:
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
        ProductEvent.Registered(
          ProductId(productId),
          "product1",
          "https://placehold.jp/123456/abcdef/150x150.png",
          100,
          "description"
        )
      )

      val name = "商品"
      actor ! Command.Edit(productId, probe.ref, nameOpt = Some(name))

      probe.expectMessage(
        ProductEvent.Edited(
          ProductId(productId),
          Some(name),
          None,
          None,
          None
        )
      )
