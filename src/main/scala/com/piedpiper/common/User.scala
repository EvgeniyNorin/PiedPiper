package com.piedpiper.common

sealed trait User {
  val sessionId: String
  val role: Role
}

case class Referer(userId: String,
                   sessionId: String,
                   name: String,
                   surname: String,
                   patronymic: Option[String],
                   role: Role = Role.REFERER) extends User

case class Reviewer(userId: String,
                    sessionId: String,
                    name: String,
                    surname: String,
                    patronymic: Option[String],
                    role: Role = Role.REVIEWER) extends User

case class Administrator(sessionId: String,
                         name: String,
                         surname: String,
                         patronymic: Option[String],
                         role: Role = Role.ADMINISTRATOR) extends User
