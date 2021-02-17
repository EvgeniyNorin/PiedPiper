package com.piedpiper.server.handlers

import java.util.UUID

import akka.http.scaladsl.model.{HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives.{entity, _}
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import com.piedpiper.dao.{UserDao, UserSessionDao}
import com.piedpiper.server.directives.AuthDirective
import com.piedpiper.server.{AuthRequest, AuthResponse, SessionDeactivateResponse}
import com.typesafe.scalalogging.Logger
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

import scala.concurrent.{ExecutionContext, Future}

class LoginHandler(userDao: UserDao, userSessionDao: UserSessionDao, authDirective: AuthDirective)(
  implicit ma: Materializer,
  executionContext: ExecutionContext,
  logger: Logger
) {
  private def getOrUpdateToken(
    authRequest: AuthRequest
  ): Future[Either[Throwable, AuthResponse]] = {
    userDao
      .find(authRequest.login, authRequest.password)
      .map(_.headOption)
      .flatMap {
        case Some(entity) =>
          val token = UUID.randomUUID().toString
          userSessionDao
            .insert(entity.userId, token)
            .map(_ => Right(AuthResponse(token, entity.userRoleType)))
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
    pathPrefix("api") {
      path("login") {
        entity(as[AuthRequest]) { authRequest: AuthRequest =>
          onSuccess(getOrUpdateToken(authRequest)) {
            case Left(th) =>
              complete(
                HttpResponse(
                  status = StatusCodes.Unauthorized,
                  entity = HttpEntity.Empty
                )
              )
            case Right(response) =>
              complete(response)
          }
        }
      } ~ path("deactivate-session") {
        authDirective.authDirective(
          user =>
            onSuccess(userSessionDao.delete(user.sessionId))(
              complete(SessionDeactivateResponse())
            )
        )
      }
    }
  }
}
