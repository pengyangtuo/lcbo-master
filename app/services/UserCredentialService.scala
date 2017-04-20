package services

import models.UserCredential

import scala.concurrent.Future
import scala.util.Try

/**
  * Created by ypeng on 2017-04-16.
  */
trait UserCredentialService {
  def create(userCred: UserCredential): Future[Either[ServiceError, UserCredential]]
  def find(email: String): Future[Either[ServiceError, Option[UserCredential]]]
  def update(email: String, oldPassword: String, newPassword: String): Future[Either[ServiceError, Option[Try[UserCredential]]]]
  def reset(email: String): Future[Either[ServiceError, Option[String]]]
}
