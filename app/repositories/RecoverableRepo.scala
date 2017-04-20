package repositories

import play.api.Logger

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by ypeng on 2017-04-16.
  */
trait RecoverableRepo {
  val logger = Logger(this.getClass)

  def recoverFromDatabaseError[T](future: Future[Either[RepositoryError, T]]) = {
    future.recover{
      case ex: Exception =>
        logger.error(ex.getMessage)
        Left(UnhandledRepositoryError(ex.getMessage))
    }
  }
}
