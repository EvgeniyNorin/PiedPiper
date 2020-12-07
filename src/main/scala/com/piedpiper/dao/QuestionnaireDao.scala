package com.piedpiper.dao

import cats.effect.IO
import cats.implicits._
import doobie.Transactor
import doobie.implicits._
import doobie.implicits.javatime._

import scala.concurrent.Future

class QuestionnaireDao()(implicit xa: Transactor[IO]) {
  def insert(questionnaire: QuestionnaireEntity): Future[Unit] =
    sql"""insert into questionnaire(text_questions) values (${questionnaire.id})""".update.run
      .transact(xa)
      .as(())
      .unsafeToFuture()
}
