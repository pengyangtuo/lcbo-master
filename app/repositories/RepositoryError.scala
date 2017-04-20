package repositories

/**
  * Created by ypeng on 2017-04-16.
  */
trait RepositoryError
case object UnavailableToConnect extends RepositoryError
case class UnhandledRepositoryError(msg: String) extends RepositoryError