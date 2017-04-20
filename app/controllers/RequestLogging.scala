package controllers

import play.api.Logger

/**
  * This trait provides a definition of log instance and a function to log incoming request
  */
trait RequestLogging {
  val log: Logger = Logger(this.getClass)

  def logIncomingRequest(requestName: String): Unit = {
    log.info(s"[incoming request]: $requestName")
  }
}
