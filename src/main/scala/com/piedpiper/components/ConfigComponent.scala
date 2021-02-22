package com.piedpiper.components

import pureconfig._
import pureconfig.generic.semiauto._

import scala.concurrent.Future

case class OracleConfig(databaseUrl: String, user: String, password: String)

case class FrontendDirectoryConfig(path: String)

case class HttpServerConfig(host: String, port: Int)

case class EmailConfig(smtpHost: String,
                       smtpPort: Int,
                       email: String,
                       password: String,
                       mailingEnabled: Boolean)

case class ConfigComponent(oracleConfig: OracleConfig,
                           frontendDirectory: FrontendDirectoryConfig,
                           httpServer: HttpServerConfig,
                           emailConfig: EmailConfig)

object ConfigComponent {
  private val frontendDirectoryEnv = "FRONTEND_DIR"
  private val serverConfigEnv = "PIPER_PORT"
  val configSource: ConfigSource = ConfigSource.default.at("app")

  implicit val emailConfigReader: ConfigReader[EmailConfig] =
    deriveReader[EmailConfig]
  implicit val oracleConfigReader: ConfigReader[OracleConfig] =
    deriveReader[OracleConfig]
  implicit val httpServerConfigReader: ConfigReader[HttpServerConfig] =
    deriveReader[HttpServerConfig].map { serverConfig =>
      sys.env
        .get(serverConfigEnv)
        .fold(serverConfig)(port => HttpServerConfig("localhost", port.toInt))
    }
  implicit val frontendDirectoryReader: ConfigReader[FrontendDirectoryConfig] =
    deriveReader[FrontendDirectoryConfig].map { frontendDirectoryConf =>
      sys.env
        .get(frontendDirectoryEnv)
        .fold(frontendDirectoryConf)(path => FrontendDirectoryConfig(path))
    }
  implicit val configComponentReader: ConfigReader[ConfigComponent] =
    deriveReader[ConfigComponent]

  def mk(): Future[ConfigComponent] = {
    configSource.load[ConfigComponent] match {
      case Left(value) =>
        Future.failed(
          new RuntimeException(s"Config loading failed: ${value.prettyPrint()}")
        )
      case Right(value) =>
        Future.successful(value)
    }
  }
}
