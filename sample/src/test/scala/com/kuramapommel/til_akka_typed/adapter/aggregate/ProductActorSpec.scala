package com.kuramapommel.til_akka_typed.adapter.aggregate

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.persistence.typed.PersistenceId
import com.kuramapommel.til_akka_typed.domain.model.event.ProductEvent
import com.kuramapommel.til_akka_typed.domain.model.valueobject.*
import io.github.iltotore.iron.*
import java.util.UUID
import org.scalatest.wordspec.AnyWordSpecLike
import scala.concurrent.ExecutionContext

class ProductPersistenceActorSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike:
  given ec: ExecutionContext = testKit.system.executionContext

  "ProductPersistenceActor" should:
    "Register コマンドを受信し処理が成功したとき Registered イベントが発生する" in:
      val productId = ProductId(UUID.randomUUID().toString())
      val actor = testKit.spawn(ProductActor: () =>
        PersistenceId.ofUniqueId(productId.value))
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

    "Edit コマンドを受信し処理が成功したとき Edited イベントが発生する" in:
      val productId = ProductId(UUID.randomUUID().toString())
      val actor = testKit.spawn(ProductActor: () =>
        PersistenceId.ofUniqueId(productId.value))
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
      actor ! Command.Edit(Some(productId.value), probe.ref, nameOpt = Some(name))
      probe.expectMessage(
        ProductEvent.Edited(
          productId,
          Some(name),
          None,
          None,
          None
        )
      )

      actor ! Command.Edit(
        Some(productId.value),
        probe.ref,
        imageUrlOpt = Some("https://placehold.jp/abcdef/123456/150x150.png")
      )
      probe.expectMessage(
        ProductEvent.Edited(
          productId,
          None,
          Some("https://placehold.jp/abcdef/123456/150x150.png"),
          None,
          None
        )
      )

      val description = "1000yen"
      actor ! Command.Edit(Some(productId.value), probe.ref, descriptionOpt = Some(description))
      probe.expectMessage(
        ProductEvent.Edited(
          productId,
          None,
          None,
          None,
          Some(description)
        )
      )

      val price = 1000
      actor ! Command.Edit(Some(productId.value), probe.ref, priceOpt = Some(price))
      probe.expectMessage(
        ProductEvent.Edited(
          productId,
          None,
          None,
          Some(price),
          None
        )
      )

    "Delete コマンドを受信し処理が成功したとき Deleted イベントが発生する" in:
      val productId = ProductId(UUID.randomUUID().toString())
      val actor = testKit.spawn(ProductActor: () =>
        PersistenceId.ofUniqueId(productId.value))
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

      actor ! Command.Delete(Some(productId.value), probe.ref)
      probe.expectMessage(
        ProductEvent.Deleted(productId, true)
      )
