package com.piedpiper.form

import com.piedpiper.form.QuestionnaireScoringService.{ScoredQuestion, TwoOptionsQuestion}
import com.piedpiper.server.QuestionnaireFormPutRequest

class QuestionnaireScoringService() {
  private val mapForScoring = Set("1", "2", "3", "4", "5")
  private val workBeforeNoonId = TwoOptionsQuestion(6, "Да")
  private val indentationCharacterId = TwoOptionsQuestion(7, "Tab")
  private val loveDogsId = TwoOptionsQuestion(8, "Да")
  private val toxicId = TwoOptionsQuestion(11, "Нет")
  private val workInHooliId = TwoOptionsQuestion(12, "Да")
  private val uselessAppsDevId = TwoOptionsQuestion(14, "Нет")
  private val fascistId = TwoOptionsQuestion(15, "Нет")
  private val hiddenFascistId = TwoOptionsQuestion(16, "Нет")
  private val beardAsHairsId = TwoOptionsQuestion(17, "Нет")
  private val tallCandidateId = TwoOptionsQuestion(18, "Нет")
  private val assemblyKnowledgeId = ScoredQuestion(20, mapForScoring)
  private val cppKnowledgeId = ScoredQuestion(21, mapForScoring)
  private val pythonKnowledgeId = ScoredQuestion(22, mapForScoring)

  private val twoOptionsQuestions = List(
    workBeforeNoonId,
    indentationCharacterId,
    loveDogsId,
    toxicId,
    workInHooliId,
    uselessAppsDevId,
    fascistId,
    hiddenFascistId,
    beardAsHairsId,
    tallCandidateId
  )

  private val scoredQuestion =
    List(assemblyKnowledgeId, cppKnowledgeId, pythonKnowledgeId)

  private val realLinkWeight = 3
  private val borderValue = 15

  def isAppropriate(req: QuestionnaireFormPutRequest,
                    isReferralLinkReal: Boolean): Boolean = {

    val initialAcc = req.multipleChoicesQuestions.foldLeft(0) { (acc, radio) =>
      acc + twoOptionsQuestions
        .find(q => q.id == radio.questionId && q.rightAnswer == radio.chosen)
        .fold(0)(_ => 1)
    }
    val res: Int = req.multipleChoicesQuestions.foldLeft(initialAcc) { (acc, radio) =>
      acc + scoredQuestion
        .find(q => q.id == radio.questionId && q.range.contains(radio.chosen))
        .fold(0)(_ => radio.chosen.toInt)
    }
     res + (if(isReferralLinkReal) 0 else realLinkWeight) >= borderValue
  }
}

object QuestionnaireScoringService {
  case class TwoOptionsQuestion(id: Long, rightAnswer: String)
  case class ScoredQuestion(id: Long, range: Set[String])
}
