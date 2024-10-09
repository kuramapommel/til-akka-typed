package com.kuramapommel.til_akka_typed.adapter.aggregate

import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.cluster.sharding.typed.scaladsl.Entity
import akka.persistence.typed.PersistenceId

object ShardedProductActor:
  def apply()(using system: ActorSystem[?]) =
    Behaviors.setup[Command]: ctx =>
      import Command.*
      val sharding = ClusterSharding(system)
      val shardregion: ActorRef[ShardingEnvelope[Command]] =
        sharding.init(
          Entity(ProductActor.typeKey)(createBehavior =
            entityContext => ProductActor(() => PersistenceId(entityContext.entityTypeKey.name, entityContext.entityId))
          )
        )

      Behaviors.receiveMessage:
        case register: Register =>
          shardregion ! ShardingEnvelope("counter-1", register)
          Behaviors.same
        case edit: Edit =>
          shardregion ! ShardingEnvelope("counter-1", edit)
          Behaviors.same
