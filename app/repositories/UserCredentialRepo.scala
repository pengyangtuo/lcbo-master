package repositories

import java.util.UUID

import models.{User, UserCredential}

import scala.concurrent.Future

/**
  * Created by ypeng on 2017-04-16.
  */
trait UserCredentialRepo {
  def create(userCred: UserCredential): Future[Either[RepositoryError, UserCredential]]
  def find(email: String): Future[Either[RepositoryError, Option[UserCredential]]]
  def update(userCred: UserCredential): Future[Either[RepositoryError, UserCredential]]
}
