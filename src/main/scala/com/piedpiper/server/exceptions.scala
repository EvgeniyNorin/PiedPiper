package com.piedpiper.server

trait PiedPiperError

trait ShowableException extends PiedPiperError

trait InternalException extends PiedPiperError

case class AuthorizationException(userId: String) extends RuntimeException(s"User with user_id = $userId must be authorized") with ShowableException

case class SessionNotFoundException(sessionId: String) extends RuntimeException(s"Session $sessionId not found") with ShowableException

case class UserNotFoundException(login: String) extends RuntimeException("User with such a combination of user/pass not found") with ShowableException

case class DatabaseModelException(entityName: String) extends RuntimeException(s"Model error with entity: $entityName") with InternalException

case class PrivilegeException() extends RuntimeException(s"Not sufficient privileges") with InternalException