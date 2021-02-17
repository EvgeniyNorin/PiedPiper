package com.piedpiper.dao

import com.piedpiper.common.{QuestionType, Role}
import com.piedpiper.server.{RadioForm, TextForm}

case class TextQuestion(question: String)

case class RadioQuestion(question: String, variants: List[String])

case class CandidateForm(textQuestions: List[TextForm],
                         multipleChoicesQuestions: List[RadioForm])

case class UserEntity(userId: String,
                      login: String,
                      password: String,
                      userName: String,
                      userSurname: String,
                      userPatronymic: Option[String],
                      userRoleType: Role)

case class SessionEntity(userId: String, sessionId: String)

case class RawCandidateEntity(candidateId: String,
                              refererId: Option[String],
                              approved: Boolean,
                              form: String)

case class CandidateEntity(candidateId: String,
                           refererId: Option[String],
                           approved: Boolean,
                           form: CandidateForm)

case class RawQuestionEntity(questionId: Long,
                             questionType: QuestionType,
                             isImmutable: Boolean,
                             content: String)

case class TextQuestionEntity(questionId: Long,
                              questionType: QuestionType,
                              isImmutable: Boolean,
                              textQuestion: TextQuestion)

case class RadioQuestionEntity(questionId: Long,
                               questionType: QuestionType,
                               isImmutable: Boolean,
                               radioQuestion: RadioQuestion)

case class LinkEntity(linkId: String,
                      refererId: String,
                      activated: Boolean)