package com.piedpiper.server

import akka.http.scaladsl.model.{HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives.{as, complete, entity, onSuccess, path, post, _}
import akka.http.scaladsl.server.Route
import com.piedpiper.dao.{QuestionnaireDao, QuestionnaireEntity}
import com.typesafe.scalalogging.Logger
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import io.circe.syntax._

import scala.concurrent.{ExecutionContext, Future}

class QuestionnaireHandler(questionsHandler: QuestionsHandler, questionnaireDao: QuestionnaireDao)(implicit executionContext: ExecutionContext, logger: Logger) {
  def fillQuestionnaire(request: QuestionnaireRequest): Future[Either[Throwable, Unit]] = {
    questionsHandler.getQuestions.flatMap {
      case Left(value) =>
        logger.error("Got error during questions fetching", value)
        throw value
      case Right(value) =>
        request.textQuestions.foreach { textQ =>
          if (!value.textQuestions.exists(_.id == textQ.id)) {
            throw new RuntimeException(s"No such id=${textQ.id} exception")
          }
        }
        questionnaireDao
          .insert(QuestionnaireEntity(request.textQuestions))
          .recover {
            case th =>
              logger.error("Got error during inserting into questionnaire table", th)
              Left(th)
          }
          .map(Right(_))
    }
  }

  val route: Route = post {
    path("questionnaire") {
      entity(as[QuestionnaireRequest]) { request: QuestionnaireRequest =>
        onSuccess(fillQuestionnaire(request)) {
          case Left(th) =>
            complete(HttpResponse(status = StatusCodes.NotFound, entity = HttpEntity.Empty))
          case Right(_) =>
            complete(HttpEntity.Empty)
        }
      }
    }
  }
}
