package com.piedpiper.server

case class AuthRequest(login: String, password: String)

case class AuthResponse(token: String)

case class TextQuestion(id: String, label: String)

case class Variant(label: String, isCorrect: Boolean)

case class MultipleChoicesQuestion(id: String, label: String, variants: Option[List[Variant]])
case class MultipleChoicesQuestionEntity(id: String, label: String, variants: List[String])

case class QuestionsResponse(
    textQuestions: List[TextQuestion],
    multipleChoicesQuestions: List[MultipleChoicesQuestionEntity]
)

case class TextQuestionRequestEntity(id: String, answer: String)
case class MultipleChoicesQuestionRequestEntity(id: String, chosen: String)

case class QuestionnaireRequest(
    textQuestions: List[TextQuestionRequestEntity],
    multipleChoicesQuestions: List[MultipleChoicesQuestionRequestEntity]
)
