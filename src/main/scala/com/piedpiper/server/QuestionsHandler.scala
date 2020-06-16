package com.piedpiper.server

import akka.http.scaladsl.model.{HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives.{as, complete, entity, onSuccess, path, _}
import akka.http.scaladsl.server.Route
import com.piedpiper.dao.QuestionsDao
import com.typesafe.scalalogging.Logger
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import enumeratum._
import io.circe.generic.auto._
import io.circe.parser.decode

import scala.concurrent.{ExecutionContext, Future}

class QuestionsHandler(questionsDao: QuestionsDao)(implicit executionContext: ExecutionContext, logger: Logger) {
  import QuestionsHandler._

  def getQuestions: Future[Either[Throwable, QuestionsResponse]] =
    questionsDao.fetchAll
      .map { questions =>
        questions.map { q =>
          QuestionType.withNameOption(q.`type`) match {
            case Some(QuestionType.Text) => Left(TextQuestion(q.id, q.label))
            case Some(QuestionType.MultipleChoices) =>
              val decodedBody = decode[Option[List[Variant]]](q.data.getOrElse("")).fold(
                error => {
                  logger.error("Got error during DATA parsing", error)
                  throw error
                },
                body => body
              )
              Right(MultipleChoicesQuestion(q.id, q.label, decodedBody))
            case _ =>
              logger.error("Got error during QuestionType parsing")
              throw new RuntimeException("Unexpected enum type")
          }
        }
      }
      .map { eitherList =>
        val text = eitherList.filter(_.isLeft).map(_.left.get)
        val multiple = eitherList.filter(_.isRight).map { m =>
          val extracted = m.right.get
          MultipleChoicesQuestionEntity(extracted.id, extracted.label, extracted.variants.getOrElse(Nil).map(_.label))
        }
        Right(QuestionsResponse(text, multiple))
      }
      .recover {
        case th =>
          logger.error("Got error during fetching from QUESTIONS Table", th)
          Left(th)
      }

  val route: Route = get {
    path("questions") {
      onSuccess(getQuestions) {
        case Left(th) =>
          complete(HttpResponse(status = StatusCodes.Unauthorized, entity = HttpEntity.Empty))
        case Right(response) =>
          complete(response)
      }
    }
  }
}

object QuestionsHandler {
  sealed abstract class QuestionType(override val entryName: String) extends EnumEntry

  object QuestionType extends Enum[QuestionType] {
    val values = findValues

    case object Text extends QuestionType("text")
    case object MultipleChoices extends QuestionType("multiple_choice")
  }
}
