package services

import java.util.UUID

import models.{User, UserCredential}

import scala.concurrent.Future

/**
  * Created by ypeng on 2017-04-16.
  */
trait UserService {
  def authenticate(email: String, password: String): Future[Either[ServiceError, Option[User]]]
  def create(user: User): Future[Either[ServiceError, User]]
  def find(id: UUID): Future[Either[ServiceError, Option[User]]]
}
