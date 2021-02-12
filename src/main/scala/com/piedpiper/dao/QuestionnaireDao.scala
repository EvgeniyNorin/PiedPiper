package com.piedpiper.dao

import cats.effect.IO
import doobie.Transactor
import cats.effect.IO
import cats.implicits._
import com.piedpiper.common.QuestionType
import doobie._
import doobie.implicits._

import scala.concurrent.{ExecutionContext, Future}


class QuestionnaireDao()(implicit xa: Transactor[IO], executionContext: ExecutionContext) {
  def getTextQuestions: Future[List[TextQuestionEntity]] = {
    sql"""select * from PIED_PIPER_QUESTIONS
         where QUESTION_TYPE='TEXT'"""
      .query[RawQuestionEntity]
      .to[List]
      .transact(xa)
      .unsafeToFuture()
      .map(_.map(QuestionnaireDao.toText))
  }

  def getRadioQuestions: Future[List[RadioQuestionEntity]] = {
    sql"""select * from PIED_PIPER_QUESTIONS
         where QUESTION_TYPE='RADIO'"""
      .query[RawQuestionEntity]
      .to[List]
      .transact(xa)
      .unsafeToFuture()
      .map(_.map(QuestionnaireDao.toRadio))
  }
}

object QuestionnaireDao {
  import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._

  def toText(rawEntity: RawQuestionEntity): TextQuestionEntity = {
    TextQuestionEntity(
      questionId = rawEntity.questionId,
      questionType = rawEntity.questionType,
      isImmutable = rawEntity.isImmutable,
      textQuestion = decode[TextQuestion](rawEntity.content).right.get
    )
  }

  def toRadio(rawEntity: RawQuestionEntity): RadioQuestionEntity = {
    RadioQuestionEntity(
      questionId = rawEntity.questionId,
      questionType = rawEntity.questionType,
      isImmutable = rawEntity.isImmutable,
      radioQuestion = decode[RadioQuestion](rawEntity.content).right.get
    )
  }
}