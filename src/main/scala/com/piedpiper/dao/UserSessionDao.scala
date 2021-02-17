package com.piedpiper.dao

import java.time.{Instant, OffsetDateTime}

import cats.effect.IO
import cats.implicits._
import doobie._
import doobie.implicits._
import doobie.implicits.legacy.instant._
import doobie.util.Put

import scala.concurrent.Future

class UserSessionDao()(implicit xa: Transactor[IO]) {
  implicit val offsetDateTimePut: Put[OffsetDateTime] =
    Put[Instant].contramap(_.toInstant)

  def find(token: String): Future[List[SessionEntity]] =
    sql"""select * from PIED_PIPER_USER_SESSION
         where TOKEN_VALUE=$token"""
      .query[SessionEntity]
      .to[List]
      .transact(xa)
      .unsafeToFuture()

  def updateSessionIdTtl(sessionId: String,
                         creationDate: OffsetDateTime): Future[Unit] =
    sql"""
          UPDATE PIED_PIPER_USER_SESSION
          SET CREATION_DATE=$creationDate
          WHERE TOKEN_VALUE=$sessionId
         """.update.run
      .transact(xa)
      .as(())
      .unsafeToFuture()

  def insert(userId: String, token: String): Future[Unit] = {
    val now: OffsetDateTime = OffsetDateTime.now()
    sql"""insert into PIED_PIPER_USER_SESSION (USER_ID, TOKEN_VALUE, CREATION_DATE)
         VALUES ($userId, $token, $now)""".update.run
      .transact(xa)
      .as(())
      .unsafeToFuture()
  }

  def delete(sessionId: String): Future[Unit] = {
    sql"""delete from PIED_PIPER_USER_SESSION where TOKEN_VALUE=$sessionId""".update.run
      .transact(xa)
      .as(())
      .unsafeToFuture()
  }
}
