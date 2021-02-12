package com.piedpiper.server.handlers

import java.util.UUID

import akka.http.scaladsl.server.Directives.{entity, get, path, pathPrefix, _}
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import com.piedpiper.dao.{CandidateDao, CandidateForm, QuestionnaireDao}
import com.piedpiper.mail.EmailService
import com.piedpiper.server.{QuestionnaireFormPutRequest, QuestionnaireFormPutResponse}
import com.typesafe.scalalogging.Logger
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

import scala.concurrent.{ExecutionContext, Future}

class QuestionnaireHandler(questionnaireDao: QuestionnaireDao,
                           emailService: EmailService,
                           candidateDao: CandidateDao)(
  implicit ma: Materializer,
  executionContext: ExecutionContext,
  logger: Logger
) {

  private val emailQuestionId = "3"

  def processForm(req: QuestionnaireFormPutRequest): Future[Unit] = {
    val email =
      req.textQuestions.find(_.questionId == emailQuestionId).map(_.answer).get
    val candidateId = UUID.randomUUID().toString
    candidateDao
      .insert(
        candidateId = candidateId,
        approved = false,
        refererId = None,
        candidateForm =
          CandidateForm(req.textQuestions, req.multipleChoicesQuestions)
      )
      .flatMap(_ => emailService.sentEmail(email))
  }

  val route: Route = get {
    pathPrefix("api") {
      path("questionnaire" / "text") {
        onSuccess(
          questionnaireDao.getTextQuestions.map(_.sortBy(_.questionId.toInt))
        ) { response =>
          complete(response)
        }
      } ~ path("questionnaire" / "radio") {
        onSuccess(
          questionnaireDao.getRadioQuestions.map(_.sortBy(_.questionId.toInt))
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
      }
    }
  }
}
