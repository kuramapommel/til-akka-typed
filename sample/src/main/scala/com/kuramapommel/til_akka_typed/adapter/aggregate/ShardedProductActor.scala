package com.kuramapommel.til_akka_typed.adapter.aggregate

import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.cluster.sharding.typed.scaladsl.Entity
import akka.persistence.typed.PersistenceId

object ShardedProductActor:
  def apply(createIdValue: () => String)(using system: ActorSystem[?]) =
    val sharding = ClusterSharding(system)
    val shardregion: ActorRef[ShardingEnvelope[Command]] =
      sharding.init(
        Entity(ProductActor.typeKey)(createBehavior =
          entityContext =>
            ProductActor: () =>
              PersistenceId(entityContext.entityTypeKey.name, entityContext.entityId)
        )
      )
    Behaviors.receiveMessage[Command]:
      case message =>
        val entityId = message.id.getOrElse(createIdValue())
        shardregion ! ShardingEnvelope(entityId, message)
        Behaviors.same
