package com.piedpiper.form

import com.piedpiper.form.QuestionnaireScoringService.{ScoredQuestion, TwoOptionsQuestion}
import com.piedpiper.server.QuestionnaireFormPutRequest

class QuestionnaireScoringService() {
  private val mapForScoring: Set[String] = Set("1", "2", "3", "4", "5")
  private val workBeforeNoonId: TwoOptionsQuestion = TwoOptionsQuestion(6, "Да")
  private val indentationCharacterId: TwoOptionsQuestion = TwoOptionsQuestion(7, "Tab")
  private val loveDogsId: TwoOptionsQuestion = TwoOptionsQuestion(8, "Да")
  private val toxicId: TwoOptionsQuestion = TwoOptionsQuestion(11, "Нет")
  private val workInHooliId: TwoOptionsQuestion = TwoOptionsQuestion(12, "Да")
  private val uselessAppsDevId: TwoOptionsQuestion = TwoOptionsQuestion(14, "Нет")
  private val fascistId: TwoOptionsQuestion = TwoOptionsQuestion(15, "Нет")
  private val hiddenFascistId: TwoOptionsQuestion = TwoOptionsQuestion(16, "Нет")
  private val beardAsHairsId: TwoOptionsQuestion = TwoOptionsQuestion(17, "Нет")
  private val tallCandidateId: TwoOptionsQuestion = TwoOptionsQuestion(18, "Нет")
  private val assemblyKnowledgeId: ScoredQuestion = ScoredQuestion(20, mapForScoring)
  private val cppKnowledgeId: ScoredQuestion = ScoredQuestion(21, mapForScoring)
  private val pythonKnowledgeId: ScoredQuestion = ScoredQuestion(22, mapForScoring)

  private val twoOptionsQuestions: Seq[TwoOptionsQuestion] = List(
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

  private val scoredQuestion: Seq[ScoredQuestion] =
    List(assemblyKnowledgeId, cppKnowledgeId, pythonKnowledgeId)

  private val realLinkWeight: Int = 3
  private val borderValue: Int = 15

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
