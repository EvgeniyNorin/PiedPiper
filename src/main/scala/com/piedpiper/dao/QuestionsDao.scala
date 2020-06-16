package com.piedpiper.dao

import cats.effect.IO
import cats.implicits._
import doobie.Transactor
import doobie.implicits._
import doobie.implicits.javatime._

import scala.concurrent.Future

class QuestionsDao()(implicit xa: Transactor[IO]) {
    def fetchAll: Future[List[QuestionEntity]] =
      sql"""select * from QUESTIONS"""
        .query[QuestionEntity]
        .to[List]
        .transact(xa)
        .unsafeToFuture()
}
