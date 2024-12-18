package com.kuramapommel.til_akka_typed

import akka.actor.ActorSystem as ClassicSystem
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.scaladsl.AkkaManagement
import akka.util.Timeout
import com.fasterxml.uuid.Generators
import com.kuramapommel.til_akka_typed.adapter.aggregate.ShardedProductActor
import com.kuramapommel.til_akka_typed.adapter.routes.ProductRoutes
import scala.concurrent.ExecutionContext
import scala.util.Failure
import scala.util.Success

//#main-class

// #start-http-server
def startHttpServer(routes: Route)(using system: ActorSystem[?]): Unit =
  // Akka HTTP still needs a classic ActorSystem to start
  import system.executionContext

  val host = system.settings.config.getString("til-akka-typed.server.host")
  val port = system.settings.config.getInt("til-akka-typed.server.port")
  val futureBinding = Http().newServerAt(host, port).bind(routes)
  futureBinding.onComplete:
    case Success(binding) =>
      val address = binding.localAddress
      system.log.info(s"Server online at http://${address.getHostString}:${address.getPort}/")
    case Failure(ex) =>
      system.log.error("Failed to bind HTTP endpoint, terminating system", ex)
      system.terminate()

// #start-http-server
@main def main: Unit =
  // #server-bootstrapping
  val rootBehavior = Behaviors.setup[Nothing]: context =>
    import akka.actor.typed.scaladsl.adapter.*
    import context.system
    given ec: ExecutionContext = context.system.executionContext
    given timeout: Timeout =
      Timeout.create(context.system.settings.config.getDuration("til-akka-typed.routes.ask-timeout"))
    given classicSystem: ClassicSystem = context.system.toClassic

    val productActor =
      context.spawn(
        ShardedProductActor: () =>
          Generators.timeBasedEpochRandomGenerator().generate().toString(),
        "ShardedProductActor"
      )
    context.watch(productActor)

    val routes = new ProductRoutes(productActor)
    startHttpServer(routes.routes)

    // akka.management が設定されているときのみ akka management を使用する
    if context.system.settings.config
        .hasPath("til-akka-typed.use-akka-management")
    then
      AkkaManagement.get(classicSystem).start()
      ClusterBootstrap.get(classicSystem).start()

    Behaviors.empty
  val system = ActorSystem[Nothing](rootBehavior, "til-akka-typed")
  // #server-bootstrapping

//#main-class
