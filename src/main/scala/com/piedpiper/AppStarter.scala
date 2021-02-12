package com.piedpiper

import akka.http.scaladsl.Http
import com.piedpiper.components.{BaseComponent, ConfigComponent, DaoComponent, HandlerComponent, ServiceComponent}
import com.piedpiper.server.GeneralRoute

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object AppStarter {
  def main(args: Array[String]): Unit = {
    val baseComponent = BaseComponent()
    import baseComponent._

    val appFuture = for {
      configComponent <- ConfigComponent.mk()
      daoComponent <- DaoComponent.mk(configComponent.oracleConfig)
      serviceComponent <- ServiceComponent.mk(configComponent)
      handlerComponent <- HandlerComponent.mk(baseComponent, daoComponent, configComponent, serviceComponent)
      piedPiperServer = new GeneralRoute(
        loginHandler = handlerComponent.loginHandler,
        userInfoHandler = handlerComponent.userInfoHandler,
        resourceHandler = handlerComponent.resourceHandler,
        createReferralLinkHandler = handlerComponent.createReferralLinkHandler,
        questionnaireHandler = handlerComponent.questionnaireHandler,
        candidatesListHandler = handlerComponent.candidatesListHandler
      )
      _ <- Http().bindAndHandle(
        handler = piedPiperServer.route,
        interface = configComponent.httpServer.host,
        port = configComponent.httpServer.port
      )
    } yield ()
    Await.result(appFuture, Duration.Inf)
  }
}
