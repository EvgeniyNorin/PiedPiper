package com.piedpiper.server.handlers

import akka.http.scaladsl.server.Directives.{entity, _}
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import com.piedpiper.common._
import com.piedpiper.dao.{CandidateDao, UserDao, UserEntity}
import com.piedpiper.server.directives.AuthDirective
import com.piedpiper.server._
import com.typesafe.scalalogging.Logger
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

import scala.concurrent.ExecutionContext

class UserInfoHandler(authDirective: AuthDirective,
                      userDao: UserDao,
                      candidateDao: CandidateDao)(
  implicit ma: Materializer,
  executionContext: ExecutionContext,
  logger: Logger
) {
  private def toUserInfoResponse(user: User): UserInfoResponse = {
    user match {
      case Referer(_, _, name, surname, patronymic, role) =>
        UserInfoResponse(
          name = name,
          surname = surname,
          patronymic = patronymic,
          role = role
        )
      case Administrator(_, name, surname, patronymic, role) =>
        UserInfoResponse(name, surname, patronymic, role)
      case Reviewer(_, _, name, surname, patronymic, role) =>
        UserInfoResponse(name, surname, patronymic, role)
    }
  }
  private def toUserResponse(user: UserEntity): Option[UserResponse] = {
    user.userRoleType match {
      case Role.REFERER =>
        Some(
          UserResponse(
            userId = user.userId,
            name = user.userName,
            surname = user.userSurname,
            patronymic = user.userPatronymic,
            role = Role.REFERER
          )
        )
      case Role.REVIEWER =>
        Some(
          UserResponse(
            userId = user.userId,
            name = user.userName,
            surname = user.userSurname,
            patronymic = user.userPatronymic,
            role = Role.REVIEWER
          )
        )
      case Role.ADMINISTRATOR => None
    }
  }

  private def toRole(role: String): Role = {
    role match {
      case "Администратор" => Role.ADMINISTRATOR
      case "Ревьюер"       => Role.REVIEWER
      case "Реферер"       => Role.REFERER
    }
  }

  val route: Route = get {
    pathPrefix("api") {
      path("user-info") {
        authDirective.authDirective(user => complete(toUserInfoResponse(user)))
      } ~ path("users") {
        authDirective.requireAdministrator(
          _ =>
            onSuccess(userDao.getUsers) { users =>
              complete(users.flatMap(toUserResponse))
          }
        )
      }
    }
  } ~ put {
    pathPrefix("api") {
      path("add-user") {
        entity(as[AddUserRequest]) { addUserRequest =>
          authDirective.requireAdministrator(
            _ =>
              onSuccess(
                userDao.insert(
                  login = addUserRequest.login,
                  password = addUserRequest.password,
                  name = addUserRequest.name,
                  surname = addUserRequest.surname,
                  patronymic = addUserRequest.patronymic,
                  role = toRole(addUserRequest.role)
                )
              )(complete(AddUserResponse()))
          )
        }
      }
    }
  } ~ post {
    pathPrefix("api") {
      path("delete-user" / Remaining) { userId =>
        authDirective.requireAdministrator(
          _ => onSuccess(userDao.delete(userId))(complete(DeleteUserResponse()))
        )
      } ~ path("approve-user" / Remaining) { candidateId =>
        authDirective.requireReviewer(
          _ =>
            onSuccess(candidateDao.setApproved(candidateId))(
              complete(ApproveResponse())
          )
        )
      } ~ path("disapprove-user" / Remaining) { candidateId =>
        authDirective.requireReviewer(
          _ =>
            onSuccess(candidateDao.deleteCandidate(candidateId))(
              complete(DisapproveResponse())
          )
        )
      }
    }
  }
}
