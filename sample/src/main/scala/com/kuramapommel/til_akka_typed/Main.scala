package com.kuramapommel.til_akka_typed

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import com.kuramapommel.til_akka_typed.adapter.aggregate.ProductActor
import com.kuramapommel.til_akka_typed.adapter.routes.ProductRoutes
import com.kuramapommel.til_akka_typed.domain.model.ProductIdGenerator
import com.kuramapommel.til_akka_typed.domain.model.valueobject.ProductId
import java.util.UUID
import scala.util.Failure
import scala.util.Success

//#main-class

// #start-http-server
def startHttpServer(routes: Route)(using system: ActorSystem[?]): Unit =
  // Akka HTTP still needs a classic ActorSystem to start
  import system.executionContext

  val futureBinding = Http().newServerAt("localhost", 8080).bind(routes)
  futureBinding.onComplete:
    case Success(binding) =>
      val address = binding.localAddress
      system.log.info("Server online at http://{}:{}/", address.getHostString, address.getPort)
    case Failure(ex) =>
      system.log.error("Failed to bind HTTP endpoint, terminating system", ex)
      system.terminate()

// #start-http-server
@main def main: Unit =
  // #server-bootstrapping
  val rootBehavior = Behaviors.setup[Nothing]: context =>
    import context.system
    val productActor =
      context.spawn(ProductActor(ProductIdGenerator(() => ProductId(UUID.randomUUID().toString()))), "ProductActor")
    context.watch(productActor)

    val routes = new ProductRoutes(productActor)
    startHttpServer(routes.routes)

    Behaviors.empty
  val system = ActorSystem[Nothing](rootBehavior, "HelloAkkaHttpServer")
  // #server-bootstrapping

//#main-class
