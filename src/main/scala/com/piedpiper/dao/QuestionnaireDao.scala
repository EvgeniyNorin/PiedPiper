package com.piedpiper.dao

import java.util.UUID

import cats.effect.IO
import doobie.Transactor
import cats.effect.IO
import cats.implicits._
import com.piedpiper.common.QuestionType
import doobie._
import doobie.implicits._

import scala.concurrent.{ExecutionContext, Future}

class QuestionnaireDao()(implicit xa: Transactor[IO],
                         executionContext: ExecutionContext) {
  def getAllTextQuestions: Future[List[TextQuestionEntity]] = {
    sql"""select * from PIED_PIPER_QUESTIONS_NEW
         where QUESTION_TYPE='TEXT'"""
      .query[RawQuestionEntity]
      .to[List]
      .transact(xa)
      .unsafeToFuture()
      .map(_.map(QuestionnaireDao.toText))
  }

  def getActiveTextQuestions: Future[List[TextQuestionEntity]] = {
    sql"""select * from PIED_PIPER_QUESTIONS_NEW
         where QUESTION_TYPE='TEXT' AND IS_ACTIVE='1'"""
      .query[RawQuestionEntity]
      .to[List]
      .transact(xa)
      .unsafeToFuture()
      .map(_.map(QuestionnaireDao.toText))
  }

  def getRadioQuestions: Future[List[RadioQuestionEntity]] = {
    sql"""select * from PIED_PIPER_QUESTIONS_NEW
         where QUESTION_TYPE='RADIO'"""
      .query[RawQuestionEntity]
      .to[List]
      .transact(xa)
      .unsafeToFuture()
      .map(_.map(QuestionnaireDao.toRadio))
  }

  def getMutableQuestions: Future[List[TextQuestionEntity]] = {
    sql"""select * from PIED_PIPER_QUESTIONS_NEW
         where QUESTION_TYPE='TEXT' AND IS_IMMUTABLE='0' AND IS_ACTIVE='1'"""
      .query[RawQuestionEntity]
      .to[List]
      .transact(xa)
      .unsafeToFuture()
      .map(_.map(QuestionnaireDao.toText))
  }

  def insert(entity: TextQuestion): Future[Unit] = {
    sql"""insert into PIED_PIPER_QUESTIONS_NEW(QUESTION_TYPE, IS_IMMUTABLE, CONTENT, IS_ACTIVE)
         VALUES ('TEXT', '0', ${QuestionnaireDao.textToString(entity)},'1')"""
      .update
      .run
      .transact(xa)
      .as(())
      .unsafeToFuture()
  }

  def markAsNonActive(id: Long): Future[Unit] = {
    sql"""UPDATE PIED_PIPER_QUESTIONS_NEW SET IS_ACTIVE='0' WHERE QUESTION_ID=$id AND IS_IMMUTABLE='0'"""
      .update
      .run
      .transact(xa)
      .as(())
      .unsafeToFuture()
  }

}

object QuestionnaireDao {
  import io.circe._, io.circe.generic.auto._, io.circe.parser._,
  io.circe.syntax._

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

  def textToString(textQuestion: TextQuestion): String = {
    textQuestion.asJson.noSpaces
  }
}
