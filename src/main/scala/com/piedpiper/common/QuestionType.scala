package com.piedpiper.common

import enumeratum.EnumEntry._
import enumeratum._

sealed trait QuestionType extends EnumEntry with Uppercase

object QuestionType extends Enum[QuestionType] with DoobieEnum[QuestionType] with CirceEnum[QuestionType] {

  val values = findValues

  case object Text extends QuestionType

  case object Radio extends QuestionType

}
