package com.piedpiper.server.handlers

import akka.http.scaladsl.model.{HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives.{as, complete, entity, onSuccess, path, pathPrefix, put}
import akka.http.scaladsl.server.Route
import com.piedpiper.server.AuthRequest

class CreateReferralLinkHandler {
  val route: Route = put {
    pathPrefix("api") {
      path("create-link") {
        complete(HttpResponse())
      }
    }
  }
}
