package com.kuramapommel.til_akka_typed.adapter.aggregate

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.actor.typed.scaladsl.AskPattern.*
import akka.persistence.testkit.scaladsl.EventSourcedBehaviorTestKit
import akka.persistence.typed.PersistenceId
import com.kuramapommel.til_akka_typed.domain.model.event.ProductEvent
import com.kuramapommel.til_akka_typed.domain.model.valueobject.*
import io.github.iltotore.iron.*
import org.scalatest.wordspec.AnyWordSpecLike

class ProductPersistenceActorSpec
    extends ScalaTestWithActorTestKit(EventSourcedBehaviorTestKit.config)
      with AnyWordSpecLike:

  "ProductPersistenceActor" should:
    "Register コマンドを受信し処理が成功したとき Registered イベントが発生する" in:
      val productId = ProductId("test-id")
      val actor = testKit.spawn(ProductActor(() => PersistenceId.ofUniqueId(productId.value)))
      val probe = testKit.createTestProbe[ProductEvent]()

      actor ! Command.Register(
        "product1",
        "https://placehold.jp/123456/abcdef/150x150.png",
        100,
        "description",
        probe.ref
      )

      probe.expectMessage(
        ProductEvent.Registered(
          productId,
          "product1",
          "https://placehold.jp/123456/abcdef/150x150.png",
          100,
          "description"
        )
      )

    "Edit(productId, Some(\"商品\", sender)) コマンドを受信し処理が成功したとき Edited イベントが発生する" in:
      val productId = ProductId("test-id")
      val actor = testKit.spawn(ProductActor(() => PersistenceId.ofUniqueId(productId.value)))
      val probe = testKit.createTestProbe[ProductEvent]()

      actor ! Command.Register(
        "product1",
        "https://placehold.jp/123456/abcdef/150x150.png",
        100,
        "description",
        probe.ref
      )

      probe.expectMessage(
        ProductEvent.Registered(
          productId,
          "product1",
          "https://placehold.jp/123456/abcdef/150x150.png",
          100,
          "description"
        )
      )

      val name = "商品"
      actor ! Command.Edit(productId.value, probe.ref, nameOpt = Some(name))

      probe.expectMessage(
        ProductEvent.Edited(
          productId,
          Some(name),
          None,
          None,
          None
        )
      )
