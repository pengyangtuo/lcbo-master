package repositories

import java.util.UUID

import models.User

import scala.concurrent.Future

/**
  * Created by ypeng on 2017-04-16.
  */
trait UserRepo {
  def create(user: User): Future[Either[RepositoryError, User]]
  def find(id: UUID): Future[Either[RepositoryError, Option[User]]]
}
