package services.interpretor

import com.google.inject.Inject
import models.UserCredential
import play.api.Logger
import repositories.UserCredentialRepo
import services.{ServiceError, SimpleServiceError, UserCredentialService}
import com.github.t3hnar.bcrypt._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Random, Success, Try}

/**
  * Created by ypeng on 2017-04-16.
  */
class UserCredentialServiceImpl @Inject()(userCredRepo: UserCredentialRepo) extends UserCredentialService{
  val log = Logger(this.getClass)

  override def create(userCred: UserCredential): Future[Either[ServiceError, UserCredential]] = {
    log.info("creating user credential: "+userCred.toString)
    userCredRepo
      .create(userCred.copy(password = userCred.password.bcrypt(4)))
      .map{
        case Right(cred) => Right(cred)
        case Left(error) => Left(SimpleServiceError)
      }
  }

  override def find(email: String): Future[Either[ServiceError, Option[UserCredential]]] = {
    userCredRepo
      .find(email)
      .map{
        case Right(credOpt) => Right(credOpt)
        case Left(error) => Left(SimpleServiceError)
      }
  }

  override def update(email: String, oldPassword: String, newPassword: String): Future[Either[ServiceError, Option[Try[UserCredential]]]] = {
    find(email)
      .flatMap{
        case Left(error) => Future.successful(Left(SimpleServiceError))
        case Right(None) => Future.successful(Right(None))
        case Right(Some(cred)) =>
          if(oldPassword.isBcrypted(cred.password)){
            userCredRepo
              .update(cred.copy(password = newPassword.bcrypt(4)))
              .map{
                case Left(err) => Left(SimpleServiceError)
                case Right(cred) => Right(Some(Success(cred)))
              }
          }else{
            Future.successful(Right(Some(Failure(new Throwable("old password does not match")))))
          }
      }
  }

  override def reset(email: String) = {
    find(email)
      .flatMap {
        case Left(error) => Future.successful(Left(SimpleServiceError))
        case Right(None) => Future.successful(Right(None))
        case Right(Some(cred)) =>
          val newPassword = Random.alphanumeric.take(10).mkString
          userCredRepo
            .update(cred.copy(password = newPassword.bcrypt(4)))
            .map {
              case Left(err) => Left(SimpleServiceError)
              case Right(cred) => Right(Some(newPassword))
            }
      }
  }
}
