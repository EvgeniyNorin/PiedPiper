package com.piedpiper.server

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.piedpiper.server.handlers._

class GeneralRoute(loginHandler: LoginHandler,
                   userInfoHandler: UserInfoHandler,
                   resourceHandler: ResourceHandler,
                   createReferralLinkHandler: CreateReferralLinkHandler,
                   questionnaireHandler: QuestionnaireHandler,
                   candidatesListHandler: CandidatesListHandler) {
  val route: Route =
    loginHandler.route ~
      userInfoHandler.route ~
      createReferralLinkHandler.route ~
      questionnaireHandler.route ~
      candidatesListHandler.route ~
      resourceHandler.route
}
