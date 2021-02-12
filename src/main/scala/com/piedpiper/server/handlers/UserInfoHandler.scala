package com.piedpiper.server.handlers

import akka.http.scaladsl.server.Directives.{complete, get, path, pathPrefix}
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import com.piedpiper.common._
import com.piedpiper.server.UserInfoResponse
import com.piedpiper.server.directives.AuthDirective
import com.typesafe.scalalogging.Logger
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

import scala.concurrent.ExecutionContext

class UserInfoHandler(authDirective: AuthDirective)
                     (implicit ma: Materializer,
                      executionContext: ExecutionContext,
                      logger: Logger) {
  private def toUserInfoResponse(user: User): UserInfoResponse = {
    user match {
      case Candidate => ???
      case Referer(userId, sessionId, name, surname, patronymic) =>
        UserInfoResponse(name, surname, patronymic, Role.Referer)
      case Reviewer(userId, sessionId) => ???
    }
  }

  val route: Route = get {
    pathPrefix("api") {
      path("user-info") {
        authDirective.authDirective(user => complete(toUserInfoResponse(user)))
      }
    }
  }
}
