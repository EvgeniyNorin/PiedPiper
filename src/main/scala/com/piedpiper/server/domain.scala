package com.piedpiper.server

import com.piedpiper.common.Role

case class AuthRequest(login: String, password: String)

case class AuthResponse(token: String, role: Role)

case class UserInfoResponse(name: String,
                            surname: String,
                            patronymic: Option[String],
                            role: Role)

case class CandidateEntityResponse(id: String, fio: String, position: String)

case class CandidatesListResponse(candidates: List[CandidateEntityResponse])

case class TextForm(questionId: String, answer: String)
case class RadioForm(questionId: String, chosen: String)

case class QuestionnaireFormPutRequest(
  textQuestions: List[TextForm],
  multipleChoicesQuestions: List[RadioForm]
)

case class QuestionnaireFormPutResponse()

case class QuestionResponse(id: String, question: String, answer: String)

case class CandidateFilledFormResponse(elements: List[QuestionResponse])
