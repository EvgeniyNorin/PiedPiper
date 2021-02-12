package com.piedpiper.components

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import cats.effect.{ContextShift, IO}
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

class BaseComponent(implicit val logger: Logger,
                    implicit val executionContext: ExecutionContextExecutor,
                    implicit val cs: ContextShift[IO],
                    implicit val actorSystem: ActorSystem,
                    implicit val materializer: ActorMaterializer)

object BaseComponent {
  def apply(): BaseComponent = {
    implicit val logger: Logger = Logger(LoggerFactory.getLogger(this.getClass))
    implicit val executionContext: ExecutionContextExecutor = ExecutionContext.global
    implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
    implicit val actorSystem: ActorSystem = ActorSystem()
    implicit val mat: ActorMaterializer = ActorMaterializer()
    new BaseComponent()
  }
}
