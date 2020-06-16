package com.piedpiper.server

import java.util.UUID

import akka.http.scaladsl.model.{HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import com.piedpiper.dao.UserDao
import com.typesafe.scalalogging.Logger
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

import scala.concurrent.{ExecutionContext, Future}

class LoginHandler(userDao: UserDao)(implicit materializer: Materializer, executionContext: ExecutionContext, logger: Logger) {
  private def getOrUpdateToken(authRequest: AuthRequest): Future[Either[Throwable, AuthResponse]] = {
    userDao
      .find(authRequest.login, authRequest.password)
      .map(_.headOption)
      .flatMap {
        case Some(entity) =>
          entity.token
            .fold[Future[String]] {
              val token = UUID.randomUUID().toString
              userDao.updateToken(authRequest.login, token).map(_ => token)
            } { tkn =>
              Future.successful(tkn)
            }
            .map(token => Right(AuthResponse(token)))
        case None =>
          logger.info(s"User not found for body: ${authRequest.toString}")
          Future.successful(Left(new RuntimeException("User entity not found")))
      }
      .recoverWith {
        case th =>
          logger.error("Got error during handling db query", th)
          Future.successful(Left(new RuntimeException("User entity not found")))
      }
  }

  val route: Route = post {
    path("login") {
      entity(as[AuthRequest]) { authRequest: AuthRequest =>
        onSuccess(getOrUpdateToken(authRequest)) {
          case Left(th)        =>
            complete(HttpResponse(status = StatusCodes.Unauthorized, entity = HttpEntity.Empty))
          case Right(response) =>
            complete(response)
        }
      }
    }
  }
}
