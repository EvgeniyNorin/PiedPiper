package com.piedpiper.server.handlers

import java.util.UUID

import akka.http.scaladsl.server.Directives.{entity, _}
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import com.piedpiper.common._
import com.piedpiper.dao.{CandidateDao, UserDao, UserEntity}
import com.piedpiper.mail.EmailService
import com.piedpiper.server.directives.AuthDirective
import com.piedpiper.server._
import com.typesafe.scalalogging.Logger
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

import scala.concurrent.{ExecutionContext, Future}

class UserInfoHandler(authDirective: AuthDirective,
                      userDao: UserDao,
                      candidateDao: CandidateDao,
                      emailService: EmailService)(
  implicit ma: Materializer,
  executionContext: ExecutionContext,
  logger: Logger
) {
  private val emailQuestionId = 3
  private val fioId = 1
  private val subject = "Работа в Pied Piper"
  private val rejectionContent = "Простите, вы нам не подходите"

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

  def handleRejection(candidateId: String): Future[Unit] = {
    candidateDao.getCandidate(candidateId).flatMap {
      _.fold(Future.successful(())) { candidate =>
        val email =
          candidate.form.textQuestions
            .find(_.questionId == emailQuestionId)
            .map(_.answer)
            .get
        val fio =
          candidate.form.textQuestions
            .find(_.questionId == fioId)
            .map(_.answer)
            .get
        emailService.sendRejection(email, fio)
      }
    }
  }

  def handleInvitation(candidateId: String): Future[Unit] = {
    candidateDao.getCandidate(candidateId).flatMap {
      _.fold(Future.successful(())) { candidate =>
        val email =
          candidate.form.textQuestions
            .find(_.questionId == emailQuestionId)
            .map(_.answer)
            .get
        val fio =
          candidate.form.textQuestions
            .find(_.questionId == fioId)
            .map(_.answer)
            .get
        emailService.sendInvitation(email, fio)
      }
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
            onSuccess(
              handleInvitation(candidateId)
                .flatMap(
                  _ =>
                    candidateDao
                      .setApproved(candidateId)
                )
            )(complete(ApproveResponse()))
        )
      } ~ path("disapprove-user" / Remaining) { candidateId =>
        authDirective.requireReviewer(
          _ =>
            onSuccess(
              handleRejection(candidateId)
                .flatMap(
                  _ =>
                    candidateDao
                      .deleteCandidate(candidateId)
                )
            )(complete(DisapproveResponse()))
        )
      }
    }
  }
}
