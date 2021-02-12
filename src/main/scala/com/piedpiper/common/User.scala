package com.piedpiper.common

import io.circe.generic.auto._

sealed trait User

case object Candidate extends User

case class Referer(userId: String,
                   sessionId: String,
                   name: String,
                   surname: String,
                   patronymic: Option[String]) extends User

case class Reviewer(userId: String, sessionId: String) extends User
