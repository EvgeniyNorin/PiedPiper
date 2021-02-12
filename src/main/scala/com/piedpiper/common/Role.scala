package com.piedpiper.common

import enumeratum.EnumEntry._
import enumeratum._

sealed trait Role extends EnumEntry with Uppercase

object Role extends Enum[Role] with DoobieEnum[Role] with CirceEnum[Role] {

  val values = findValues

  case object Referer extends Role

  case object Reviewer extends Role

  case object Administrator extends Role

}
