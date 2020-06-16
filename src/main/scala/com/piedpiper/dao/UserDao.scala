package com.piedpiper.dao

import cats.effect.IO
import doobie._
import doobie.implicits._
import cats.implicits._
import cats.effect.IO
import doobie.implicits.javatime._

import scala.concurrent.Future

class UserDao()(implicit xa: Transactor[IO]) {
  def find(login: String, password: String): Future[List[UserEntity]] =
    sql"""select * from PIED_PIPER_USER 
         where LOGIN_VALUE=$login 
         and PASSWORD_VALUE=$password"""
      .query[UserEntity]
      .to[List]
      .transact(xa)
      .unsafeToFuture()

  def updateToken(login: String, token: String): Future[Unit] =
    sql"""
          UPDATE PIED_PIPER_USER
          SET TOKEN_VALUE=$token
          WHERE LOGIN_VALUE=$login
         """
      .update
      .run
      .transact(xa)
      .as(())
      .unsafeToFuture()
}
