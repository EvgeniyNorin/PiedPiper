package com.piedpiper.server.handlers

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.piedpiper.components.FrontendDirectoryConfig

class ResourceHandler(frontendDirectoryConfig: FrontendDirectoryConfig) {
  val route: Route = {
    pathEndOrSingleSlash {
      getFromFile(frontendDirectoryConfig.path + "/index.html")
    } ~
      getFromDirectory(frontendDirectoryConfig.path)
  }
}
