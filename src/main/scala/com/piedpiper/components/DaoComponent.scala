package com.piedpiper.components

import cats.effect.{ContextShift, IO}
import com.piedpiper.dao.{CandidateDao, QuestionnaireDao, ReferralLinksDao, UserDao, UserSessionDao}
import doobie.util.transactor.Transactor
import doobie.util.transactor.Transactor.Aux

import scala.concurrent.{ExecutionContext, Future}

class DaoComponent(val userDao: UserDao,
                   val userSessionDao: UserSessionDao,
                   val candidateDao: CandidateDao,
                   val questionnaireDao: QuestionnaireDao,
                   val referralLinksDao: ReferralLinksDao)

object DaoComponent {
  def mk(oracleConfig: OracleConfig)(implicit cs: ContextShift[IO], ex: ExecutionContext): Future[DaoComponent] = {
    implicit val xa: Aux[IO, Unit] = Transactor.fromDriverManager[IO](
      driver = "oracle.jdbc.OracleDriver",
      url = oracleConfig.databaseUrl,
      user = oracleConfig.user,
      pass = oracleConfig.password
    )
    Future.successful(new DaoComponent(
      new UserDao(),
      new UserSessionDao(),
      new CandidateDao(),
      new QuestionnaireDao(),
      new ReferralLinksDao()
    ))
  }
}
