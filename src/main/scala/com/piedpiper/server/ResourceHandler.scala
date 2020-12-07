package com.piedpiper.server

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

class ResourceHandler {
  val route: Route =
    pathPrefix("") {
      pathEndOrSingleSlash {
        getFromFile("src/main/resources/dist/index.html")
      } ~
        getFromDirectory("src/main/resources/dist")
    }
}
