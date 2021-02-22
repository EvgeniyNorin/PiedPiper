package com.piedpiper.mail

import courier._
import Defaults._
import com.piedpiper.components.ConfigComponent
import javax.mail.internet.InternetAddress

import scala.concurrent.Future

class EmailService(configComponent: ConfigComponent) {

  private val subject = "Работа в Pied Piper"

  lazy val mailer: Mailer = Mailer(configComponent.emailConfig.smtpHost, configComponent.emailConfig.smtpPort)
    .auth(true)
    .as(configComponent.emailConfig.email, configComponent.emailConfig.password)
    .startTls(true)()


  def sendRejection(email: String, fio: String): Future[Unit] = {
    sentEmail(email, subject,s"Уважаемый $fio, сожалеем, но вы нам не подходите")
  }

  def sendInvitation(email: String, fio: String): Future[Unit] = {
    sentEmail(email, subject,s"Уважаемый $fio, рады вам сообщить, что вы официально приглашены на собеседование. Свяжитесь с нашим HR.")
  }

  private def sentEmail(email: String, subject: String, content: String): Future[Unit] = {
    if (configComponent.emailConfig.mailingEnabled) {
      mailer(
        Envelope
          .from(new InternetAddress(configComponent.emailConfig.email))
          .to(new InternetAddress(email))
          .subject(subject)
          .content(Text(content))
      )
    } else {
      Future.successful(())
    }
  }

}
