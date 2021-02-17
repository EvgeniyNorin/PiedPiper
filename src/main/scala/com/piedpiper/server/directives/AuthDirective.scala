package com.piedpiper.server.directives

import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives.{headerValueByName, onSuccess}
import akka.http.scaladsl.server.directives.BasicDirectives._
import com.piedpiper.common.{
  Administrator,
  Referer,
  Reviewer,
  Role,
  User
}
import com.piedpiper.dao.{SessionEntity, UserDao, UserEntity, UserSessionDao}
import com.piedpiper.server.{
  DatabaseModelException,
  PrivilegeException,
  SessionNotFoundException
}

import scala.concurrent.{ExecutionContext, Future}

class AuthDirective(userDao: UserDao, userSessionDao: UserSessionDao)(
  implicit executionContext: ExecutionContext
) {
  private def makeReferer(userEntity: UserEntity,
                          sessionEntity: SessionEntity): Referer = Referer(
    userId = userEntity.userId,
    sessionId = sessionEntity.sessionId,
    name = userEntity.userName,
    surname = userEntity.userSurname,
    patronymic = userEntity.userPatronymic
  )

  private def makeReviewer(userEntity: UserEntity,
                           sessionEntity: SessionEntity): Reviewer =
    Reviewer(
      userId = userEntity.userId,
      sessionId = sessionEntity.sessionId,
      name = userEntity.userName,
      surname = userEntity.userSurname,
      patronymic = userEntity.userPatronymic
    )

  private def makeAdministrator(userEntity: UserEntity,
                                sessionEntity: SessionEntity): Administrator =
    Administrator(
      sessionId = sessionEntity.sessionId,
      name = userEntity.userName,
      surname = userEntity.userSurname,
      patronymic = userEntity.userPatronymic
    )

  def authDirective: Directive1[User] = {
    headerValueByName("SESSION-ID")
      .flatMap { sessionId =>
        onSuccess(userSessionDao.find(sessionId).flatMap { sessionEntities =>
          sessionEntities.headOption match {
            case Some(sessionEntity) =>
              userDao.find(sessionEntity.userId).flatMap {
                userEntities =>
                  userEntities.headOption match {
                    case Some(userEntity) =>
                      userEntity.userRoleType match {
                        case Role.REFERER =>
                          Future
                            .successful(makeReferer(userEntity, sessionEntity))
                        case Role.REVIEWER =>
                          Future
                            .successful(makeReviewer(userEntity, sessionEntity))
                        case Role.ADMINISTRATOR =>
                          Future.successful(
                            makeAdministrator(userEntity, sessionEntity)
                          )
                      }
                    case None =>
                      Future.failed(DatabaseModelException("UserEntity"))
                  }
              }
            case None => Future.failed(SessionNotFoundException(sessionId))
          }
        })
      }
      .flatMap(provide)
  }

  def requireAdministrator: Directive1[Administrator] = {
    authDirective.map {
      case admin: Administrator => admin
      case anotherRole          => throw PrivilegeException()
    }
  }

  def requireReferer: Directive1[Referer] = {
    authDirective.map {
      case referer: Referer => referer
      case anotherRole      => throw PrivilegeException()
    }
  }

  def requireReviewer: Directive1[Reviewer] = {
    authDirective.map {
      case referer: Reviewer => referer
      case anotherRole       => throw PrivilegeException()
    }
  }
}
