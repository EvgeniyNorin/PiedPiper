package com.piedpiper.components

import com.piedpiper.mail.EmailService

import scala.concurrent.Future

class ServiceComponent(val emailService: EmailService)

object ServiceComponent {
  def mk(configComponent: ConfigComponent): Future[ServiceComponent] = {
    Future.successful(
      new ServiceComponent(
        emailService = new EmailService()
      )
    )
  }
}
