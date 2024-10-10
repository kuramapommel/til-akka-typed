package com.kuramapommel.til_akka_typed.adapter.aggregate

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import com.kuramapommel.til_akka_typed.domain.model.event.ProductEvent
import com.kuramapommel.til_akka_typed.domain.model.valueobject.*
import com.typesafe.config.ConfigFactory
import io.github.iltotore.iron.*
import java.util.UUID
import org.scalatest.wordspec.AnyWordSpecLike
import scala.concurrent.ExecutionContext

class ShardedProductActorSpec
    extends ScalaTestWithActorTestKit(
      ActorTestKit(
        "ShardedProductActorSpec",
        ConfigFactory.parseString(
          """|
             |akka.persistence.journal.plugin = "akka.persistence.journal.inmem"
             |akka.persistence.journal.inmem.class = "akka.persistence.journal.inmem.InmemJournal"
             |akka.actor.provider = "cluster"
             |akka.remote.artery.canonical.port = 25510
             |akka.cluster.seed-nodes = ["akka://ShardedProductActorSpec@127.0.0.1:25510"]
             |""".stripMargin
        )
      )
    )
      with AnyWordSpecLike:
  given ec: ExecutionContext = testKit.system.executionContext

  "ShardedProductActorSpec" should:
    "Register コマンドを受信し処理が成功したとき Registered イベントが発生する" in:
      val productId = ProductId(UUID.randomUUID().toString())
      val actor = testKit.spawn(ShardedProductActor: () =>
        productId.value)
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
      val actor = testKit.spawn(ShardedProductActor: () =>
        productId.value)
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
