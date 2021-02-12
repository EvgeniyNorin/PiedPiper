package com.piedpiper.server.directives

import akka.http.scaladsl.server.{Directive, Directive1, RequestContext}
import akka.http.scaladsl.server.Directives.headerValueByName
import akka.http.scaladsl.server.Directives.{as, complete, entity, get, headerValueByName, onSuccess, path, pathPrefix}
import akka.http.scaladsl.server.directives.BasicDirectives._
import com.piedpiper.common.{Referer, Reviewer, Role, User}
import com.piedpiper.dao.{SessionEntity, UserDao, UserEntity, UserSessionDao}
import com.piedpiper.server.{DatabaseModelException, SessionNotFoundException}

import scala.concurrent.{ExecutionContext, Future}

class AuthDirective(userDao: UserDao, userSessionDao: UserSessionDao)(implicit executionContext: ExecutionContext) {
  private def makeReferer(userEntity: UserEntity, sessionEntity: SessionEntity): Referer = Referer(
    userId = userEntity.userId,
    sessionId = sessionEntity.sessionId,
    name = userEntity.userName,
    surname = userEntity.userSurname,
    patronymic = userEntity.userPatronymic
  )

  private def makeReviewer(userEntity: UserEntity, sessionEntity: SessionEntity): Reviewer = Reviewer(
    userId = userEntity.userId,
    sessionId = sessionEntity.sessionId
  )

  def authDirective: Directive1[User] = {
    headerValueByName("SESSION-ID").flatMap {
      sessionId =>
        onSuccess(
          userSessionDao.find(sessionId).flatMap {
            sessionEntities =>
              sessionEntities.headOption match {
                case Some(sessionEntity) =>
                  userDao.find(sessionEntity.userId).flatMap {
                    userEntities =>
                      userEntities.headOption match {
                        case Some(userEntity) =>
                          userEntity.userRoleType match {
                            case Role.Referer =>
                              Future.successful(makeReferer(userEntity, sessionEntity))
                            case Role.Reviewer =>
                              Future.successful(makeReviewer(userEntity, sessionEntity))
                            case Role.Administrator => ???
                          }
                        case None => Future.failed(DatabaseModelException("UserEntity"))
                      }
                  }
                case None => Future.failed(SessionNotFoundException(sessionId))
              }
          }
        )
    }
      .flatMap(provide)
  }
}
