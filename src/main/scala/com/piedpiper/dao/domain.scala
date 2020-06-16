package com.piedpiper.dao

case class UserEntity(
    login: String,
    password: String,
    token: Option[String]
)

case class QuestionEntity(id: String, label: String, `type`: String, data: Option[String])

case class QuestionnaireEntity(id: String, answer: String, answerId: String)
