package com.piedpiper.server

import com.piedpiper.common.Role

case class AuthRequest(login: String, password: String)

case class AuthResponse(token: Option[String], role: Option[Role])

case class UserInfoResponse(name: String,
                            surname: String,
                            patronymic: Option[String],
                            role: Role)

case class UserResponse(userId: String,
                        name: String,
                        surname: String,
                        patronymic: Option[String],
                        role: Role)

case class CandidateEntityResponse(id: String, fio: String, position: String)

case class CandidatesListResponse(candidates: List[CandidateEntityResponse])

case class TextForm(questionId: Long, answer: String)
case class RadioForm(questionId: Long, chosen: String)

case class QuestionnaireFormPutRequest(
  textQuestions: List[TextForm],
  multipleChoicesQuestions: List[RadioForm],
  linkId: Option[String]
)

case class QuestionnaireFormPutResponse()

case class QuestionResponse(id: Long, question: String, answer: String)

case class CandidateFilledFormResponse(elements: List[QuestionResponse])

case class AddUserRequest(login: String,
                          password: String,
                          role: String,
                          name: String,
                          patronymic: String,
                          surname: String)

case class AddUserResponse()

case class DeleteUserResponse()

case class SessionDeactivateResponse()

case class CreateLinkResponse(linkId: String)

case class ApproveResponse()

case class DisapproveResponse()

case class AddTextQuestionRequest(question: String)

case class AddTextQuestionResponse()

case class DeactivateTextResponse()