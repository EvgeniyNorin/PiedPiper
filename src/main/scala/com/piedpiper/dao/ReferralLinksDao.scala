package com.piedpiper.dao

import cats.effect.IO
import doobie.Transactor
import cats.effect.IO
import cats.implicits._
import com.piedpiper.common.QuestionType
import doobie._
import doobie.implicits._

import scala.concurrent.Future

class ReferralLinksDao(implicit xa: Transactor[IO]) {

  def insert(userId: String, linkId: String, activated: Boolean): Future[Unit] = {
    sql"""insert into PIED_PIPER_REFERRAL_LINKS(LINK_ID, REFERER_ID, ACTIVATED)
         VALUES ($linkId, $userId, $activated)"""
      .update
      .run
      .transact(xa)
      .as(())
      .unsafeToFuture()
  }

  def find(linkId: String): Future[List[LinkEntity]] = {
    sql"""select * from PIED_PIPER_REFERRAL_LINKS where LINK_ID=$linkId and ACTIVATED='0'"""
      .query[LinkEntity]
      .to[List]
      .transact(xa)
      .unsafeToFuture()
  }

  def setActivated(linkId: String): Future[Unit] = {
    sql"""UPDATE PIED_PIPER_REFERRAL_LINKS SET ACTIVATED='1' WHERE LINK_ID=$linkId"""
      .update
      .run
      .transact(xa)
      .as(())
      .unsafeToFuture()
  }
}
