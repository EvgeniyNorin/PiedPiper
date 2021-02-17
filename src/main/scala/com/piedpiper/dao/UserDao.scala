package com.piedpiper.dao

import java.util.UUID

import cats.effect.IO
import com.piedpiper.common.Role
import cats.implicits._
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

  def getUsers: Future[List[UserEntity]] =
    sql"""select * from PIED_PIPER_USER"""
      .query[UserEntity]
      .to[List]
      .transact(xa)
      .unsafeToFuture()

  def insert(login: String,
             password: String,
             name: String,
             surname: String,
             patronymic: String,
             role: Role): Future[Unit] = {
    val uuid = UUID.randomUUID().toString
    sql"""
         insert into PIED_PIPER_USER(USER_ID, LOGIN_VALUE, PASSWORD_VALUE, USER_NAME, USER_SURNAME, USER_PATRONYMIC, USER_ROLE_TYPE)
         VALUES ($uuid, $login, $password, $name, $surname, $patronymic, ${role.toString})
         """.update.run
      .transact(xa)
      .as(())
      .unsafeToFuture()
  }

  def delete(userId: String): Future[Unit] =
    sql"""DELETE FROM PIED_PIPER_USER WHERE USER_ID=$userId""".update.run
      .transact(xa)
      .as(())
      .unsafeToFuture()

}
