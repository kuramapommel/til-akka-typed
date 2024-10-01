package com.kuramapommel.til_akka_typed.adapter.aggregate

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.actor.typed.scaladsl.AskPattern.*
import akka.persistence.testkit.scaladsl.EventSourcedBehaviorTestKit
import com.kuramapommel.til_akka_typed.domain.model.ProductIdGenerator
import com.kuramapommel.til_akka_typed.domain.model.event.ProductEvent
import com.kuramapommel.til_akka_typed.domain.model.valueobject.*
import com.typesafe.config.ConfigFactory
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
      val productId = ProductId("test-id")
      val idGenerator = ProductIdGenerator: () =>
        productId
      val actor = testKit.spawn(ProductActor(idGenerator))
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
      val idGenerator = ProductIdGenerator: () =>
        productId
      val actor = testKit.spawn(ProductActor(idGenerator))
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

class ProductPersistenceActorSpec
    extends ScalaTestWithActorTestKit(
      EventSourcedBehaviorTestKit.config.withFallback(
        ConfigFactory.parseString(
          """
            |akka.actor {
            |  allow-java-serialization = yes
            |  warn-about-java-serializer-usage = false
            |}
            |""".stripMargin
        )
      )
    )
      with AnyWordSpecLike:

  "ProductPersistenceActor" should:
    "Register コマンドを受信し処理が成功したとき Registered イベントが発生する" in:
      val productId = ProductId("test-id")
      val actor = testKit.spawn(ProductActor(productId))
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
