package com.piedpiper.mail

import courier._
import Defaults._

import scala.concurrent.Future

class EmailService {

  private val mailer = Mailer("smtp.gmail.com", 587)
    .auth(true)
    .as("evgeniy.norin@gmail.com", "hd9923541")
    .startTls(true)()

  def sentEmail(email: String): Future[Unit] = {
    mailer(
      Envelope
        .from("evgeniy.norin" `@` "gmail.com")
        .to("evgeniy.norin" `@` "gmail.com")
        .subject("miss you")
        .content(Text("hi bro"))
    )
  }


}
