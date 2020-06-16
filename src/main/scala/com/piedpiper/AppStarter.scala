package com.piedpiper

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import cats.effect.{ContextShift, IO}
import com.piedpiper.dao.{QuestionnaireDao, QuestionsDao, UserDao}
import com.piedpiper.server.{GeneralRoute, LoginHandler, QuestionnaireHandler, QuestionsHandler}
import com.typesafe.scalalogging.Logger
import doobie.util.transactor.Transactor
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext

object AppStarter {
  def main(args: Array[String]): Unit = {
    implicit val logger = Logger(LoggerFactory.getLogger(this.getClass))

    implicit val executionContext = ExecutionContext.global
    implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

    implicit val actorSystem: ActorSystem = ActorSystem()
    implicit val materializer: ActorMaterializer = ActorMaterializer()

    implicit val xa = Transactor.fromDriverManager[IO](
      driver = "org.postgresql.Driver",
      url = "jdbc:postgresql://34.90.231.206:5432/postgres",
      user = "myuser",
      pass = "mypass"
    )

    val userDao = new UserDao()
    val questionsDao = new QuestionsDao()
    val questionnaireDao = new QuestionnaireDao()

    val loginHandler = new LoginHandler(userDao)
    val questionsHandler = new QuestionsHandler(questionsDao)
    val questionnaireHandler = new QuestionnaireHandler(questionsHandler, questionnaireDao)
    val piedPiperServer = new GeneralRoute(loginHandler, questionsHandler, questionnaireHandler)
    Http().bindAndHandle(piedPiperServer.route, "localhost", 8080)
  }
}
