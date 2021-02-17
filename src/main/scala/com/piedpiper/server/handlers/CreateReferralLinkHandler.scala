package com.piedpiper.server.handlers

import java.util.UUID

import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.piedpiper.dao.ReferralLinksDao
import com.piedpiper.server.CreateLinkResponse
import com.piedpiper.server.directives.AuthDirective
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

class CreateReferralLinkHandler(referralLinksDao: ReferralLinksDao,
                                authDirective: AuthDirective) {
  val route: Route = put {
    pathPrefix("api") {
      path("create-link") {
        authDirective.requireReferer { referer =>
          val linkId = UUID.randomUUID().toString
          onSuccess(
            referralLinksDao.insert(
              userId = referer.userId,
              linkId = linkId,
              activated = false
            )
          )(complete(CreateLinkResponse(linkId)))
        }
      }
    }
  }
}
