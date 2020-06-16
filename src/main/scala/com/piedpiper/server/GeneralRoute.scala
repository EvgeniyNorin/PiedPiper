package com.piedpiper.server

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

class GeneralRoute(loginHandler: LoginHandler,
                   questionsHandler: QuestionsHandler,
                   questionnaireHandler: QuestionnaireHandler) {
  val route: Route = loginHandler.route ~ questionsHandler.route ~ questionnaireHandler.route
}
