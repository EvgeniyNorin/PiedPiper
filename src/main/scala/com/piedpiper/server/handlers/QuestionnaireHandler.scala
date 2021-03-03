package com.piedpiper.server.handlers

import java.util.UUID

import akka.http.scaladsl.server.Directives.{entity, get, path, pathPrefix, _}
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import com.piedpiper.dao._
import com.piedpiper.form.QuestionnaireScoringService
import com.piedpiper.mail.EmailService
import com.piedpiper.server._
import com.piedpiper.server.directives.AuthDirective
import com.typesafe.scalalogging.Logger
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

import scala.concurrent.{ExecutionContext, Future}

class QuestionnaireHandler(questionnaireDao: QuestionnaireDao,
                           referralLinksDao: ReferralLinksDao,
                           emailService: EmailService,
                           candidateDao: CandidateDao,
                           authDirective: AuthDirective,
                           scoringService: QuestionnaireScoringService)(
  implicit ma: Materializer,
  executionContext: ExecutionContext,
  logger: Logger
) {

  private val emailQuestionId = 3
  private val fioId = 1

  private def processForm(req: QuestionnaireFormPutRequest): Future[Unit] = {
    val email =
      req.textQuestions.find(_.questionId == emailQuestionId).map(_.answer).get
    val fio =
      req.textQuestions.find(_.questionId == fioId).map(_.answer).get
    val candidateId = UUID.randomUUID().toString

    def insert(refererId: Option[String]): Future[Unit] = {
      candidateDao
        .insert(
          candidateId = candidateId,
          approved = false,
          refererId = refererId,
          candidateForm =
            CandidateForm(req.textQuestions, req.multipleChoicesQuestions)
        )
    }

    val isReferralLinkRealF: Future[Boolean] =
      req.linkId.fold(insert(None).map(_ => false))(
        id =>
          referralLinksDao
            .find(id)
            .recoverWith {
              case _ => Future.successful(List())
            }
            .flatMap(
              _.headOption
                .fold[Future[Boolean]](insert(None).map(_ => false))(
                  link =>
                    insert(Some(link.refererId))
                      .flatMap(_ => referralLinksDao.setActivated(link.linkId))
                      .map(_ => true)
                )
                .recover {
                  case _ => false
                }
          )
      )
    for {
      isReferralLinkReal <- isReferralLinkRealF
      isAppropriate = scoringService.isAppropriate(req, isReferralLinkReal)
      _ <- if (isAppropriate) Future.successful(())
      else
        emailService
          .sendRejection(email, fio)
          .flatMap(_ => candidateDao.deleteCandidate(candidateId))
    } yield ()
  }

  val route: Route = get {
    pathPrefix("api") {
      path("questionnaire" / "text") {
        onSuccess(
          questionnaireDao.getActiveTextQuestions.map(_.sortBy(_.questionId))
        ) { response =>
          complete(response)
        }
      } ~ path("questionnaire" / "radio") {
        onSuccess(
          questionnaireDao.getRadioQuestions.map(_.sortBy(_.questionId))
        ) { response =>
          complete(response)
        }
      } ~ path("questionnaire" / "mutable-text") {
        onSuccess(
          questionnaireDao.getMutableQuestions.map(_.sortBy(_.questionId))
        ) { response =>
          complete(response)
        }
      }
    }
  } ~ put {
    pathPrefix("api") {
      path("questionnaire" / "add") {
        entity(as[QuestionnaireFormPutRequest]) { request =>
          onSuccess(processForm(request)) {
            complete(QuestionnaireFormPutResponse())
          }
        }
      } ~ path("questionnaire" / "add-text") {
        authDirective.requireAdministrator(
          _ =>
            entity(as[AddTextQuestionRequest]) { request =>
              onSuccess(questionnaireDao.insert(TextQuestion(request.question))) {
                complete(AddTextQuestionResponse())
              }
          }
        )
      }
    }
  } ~ post {
    pathPrefix("api") {
      path("questionnaire" / "deactivate-text" / LongNumber) { questionId =>
        authDirective.requireAdministrator(
          _ =>
            onSuccess(questionnaireDao.markAsNonActive(questionId)) {
              complete(DeactivateTextResponse())
          }
        )
      }
    }
  }
}
