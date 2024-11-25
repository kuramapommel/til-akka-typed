package com.kuramapommel.til_akka_typed

import akka.actor.ActorSystem
import scala.concurrent.Future

/**
 * k8s 向けヘルスチェッカー
 * 参照：https://doc.akka.io/libraries/akka-management/current/healthchecks.html#defining-a-health-check
 *
 * @param system
 *     アクターシステム
 */
class HealthChecker(system: ActorSystem) extends (() => Future[Boolean]):

  /** 稼働中 */
  override def apply(): Future[Boolean] =
    Future.successful(true)
