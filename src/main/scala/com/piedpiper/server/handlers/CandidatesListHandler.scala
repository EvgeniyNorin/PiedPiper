package com.piedpiper.server.handlers

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import com.piedpiper.dao._
import com.piedpiper.server.{CandidateEntityResponse, CandidateFilledFormResponse, CandidatesListResponse, QuestionResponse}
import com.typesafe.scalalogging.Logger
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

import scala.concurrent.{ExecutionContext, Future}

class CandidatesListHandler(questionnaireDao: QuestionnaireDao,
                            candidateDao: CandidateDao)(
  implicit ma: Materializer,
  executionContext: ExecutionContext,
  logger: Logger
) {
  private val fioQuestionId = "1"
  private val positionQuestionId = "2"

  private def toCandidateList(
    candidateEntities: List[CandidateEntity]
  ): CandidatesListResponse = {
    CandidatesListResponse(candidateEntities.map { candidate =>
      CandidateEntityResponse(
        id = candidate.candidateId,
        fio = candidate.form.textQuestions
          .find(_.questionId == fioQuestionId)
          .get
          .answer,
        position = candidate.form.textQuestions
          .find(_.questionId == positionQuestionId)
          .get
          .answer
      )
    })
  }

  private def toCandidateForm(
    candidateId: String
  ): Future[CandidateFilledFormResponse] = {
    def toTextQuestionResponse(
      candidate: CandidateEntity,
      questions: List[TextQuestionEntity]
    ): List[QuestionResponse] = {
      candidate.form.textQuestions.map { textForm =>
        QuestionResponse(
          id = textForm.questionId,
          question = questions
            .find(_.questionId == textForm.questionId)
            .get
            .textQuestion
            .question,
          answer = textForm.answer
        )
      }
    }

    def toRadioQuestionResponse(
      candidate: CandidateEntity,
      questions: List[RadioQuestionEntity]
    ): List[QuestionResponse] = {
      candidate.form.multipleChoicesQuestions.map { radioForm =>
        val radio = questions
          .find(_.questionId == radioForm.questionId)
          .get
          .radioQuestion
        QuestionResponse(
          id = radioForm.questionId,
          question = radio.question,
          answer = radioForm.chosen
        )
      }
    }

    for {
      candidate <- candidateDao.getCandidate(candidateId).map(_.get)
      textQuestions <- questionnaireDao.getTextQuestions
      radioQuestions <- questionnaireDao.getRadioQuestions
    } yield
      CandidateFilledFormResponse(
        toTextQuestionResponse(candidate, textQuestions) ++
          toRadioQuestionResponse(candidate, radioQuestions)
      )
  }

  val route: Route = get {
    pathPrefix("api") {
      path("candidates" / "list") {
        onSuccess(candidateDao.getCandidatesList.map(toCandidateList))(
          res => complete(res)
        )
      } ~
        path("candidates" / Remaining) { candidateId =>
          onSuccess(toCandidateForm(candidateId))(res => complete(res))
        }
    }
  }
}
