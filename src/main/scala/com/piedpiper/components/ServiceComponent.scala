package com.piedpiper.components

import com.piedpiper.form.QuestionnaireScoringService
import com.piedpiper.mail.EmailService

import scala.concurrent.Future

class ServiceComponent(val emailService: EmailService, val questionnaireScoringService: QuestionnaireScoringService)

object ServiceComponent {
  def mk(configComponent: ConfigComponent): Future[ServiceComponent] = {
    Future.successful(
      new ServiceComponent(
        emailService = new EmailService(configComponent),
        questionnaireScoringService = new QuestionnaireScoringService()
      )
    )
  }
}
