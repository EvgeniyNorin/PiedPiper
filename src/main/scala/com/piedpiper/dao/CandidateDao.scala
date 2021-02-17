package com.piedpiper.dao

import cats.effect.IO
import cats.implicits._
import doobie._
import doobie.implicits._

import scala.concurrent.{ExecutionContext, Future}
import CandidateDao._

class CandidateDao()(implicit xa: Transactor[IO], ex: ExecutionContext) {

  def insert(candidateId: String,
             approved: Boolean,
             refererId: Option[String],
             candidateForm: CandidateForm
            ): Future[Unit] = {
    sql"""insert into PIED_PIPER_CANDIDATE (CANDIDATE_ID, REFERER_ID, APPROVED, FORM)
         VALUES ($candidateId, $refererId, $approved, ${candidateFormToJson(candidateForm)})"""
      .update
      .run
      .transact(xa)
      .as(())
      .unsafeToFuture()
  }

  def getCandidatesList: Future[List[CandidateEntity]] = {
    sql"""select * from PIED_PIPER_CANDIDATE
         where APPROVED='0'"""
      .query[RawCandidateEntity]
      .to[List]
      .transact(xa)
      .unsafeToFuture()
      .map(_.map(toCandidateForm))
  }

  def getCandidate(candidateId: String): Future[Option[CandidateEntity]] = {
    sql"""select * from PIED_PIPER_CANDIDATE
         where APPROVED='0' AND CANDIDATE_ID=$candidateId"""
      .query[RawCandidateEntity]
      .to[List]
      .transact(xa)
      .unsafeToFuture()
      .map(_.map(toCandidateForm).headOption)
  }

  def setApproved(candidateId: String): Future[Unit] = {
    sql"""UPDATE PIED_PIPER_CANDIDATE SET APPROVED='1' WHERE CANDIDATE_ID=$candidateId"""
      .update
      .run
      .transact(xa)
      .as(())
      .unsafeToFuture()
  }

  def deleteCandidate(candidateId: String): Future[Unit] = {
    sql"""DELETE FROM PIED_PIPER_CANDIDATE WHERE CANDIDATE_ID=$candidateId"""
      .update
      .run
      .transact(xa)
      .as(())
      .unsafeToFuture()
  }
}

object CandidateDao {
  import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._

  def candidateFormToJson(candidateForm: CandidateForm): String = {
    candidateForm.asJson.noSpaces
  }

  def toCandidateForm(rawCandidateForm: RawCandidateEntity): CandidateEntity = {
    CandidateEntity(
      candidateId = rawCandidateForm.candidateId,
      refererId = rawCandidateForm.refererId,
      approved = rawCandidateForm.approved,
      form = decode[CandidateForm](rawCandidateForm.form).right.get
    )
  }
}