package com.piedpiper.dao

import cats.effect.IO
import doobie._
import doobie.implicits._

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

  def find(userId: String): Future[List[UserEntity]] =
    sql"""select * from PIED_PIPER_USER
         where USER_ID=$userId"""
      .query[UserEntity]
      .to[List]
      .transact(xa)
      .unsafeToFuture()
}
