package services.interpretor

import java.util.UUID

import com.github.t3hnar.bcrypt._
import com.google.inject.Inject
import models.{User, UserCredential}
import play.api.Logger
import repositories.UserRepo
import services._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by ypeng on 2017-04-16.
  */
class UserServiceImpl @Inject()(userRepo: UserRepo, userCredentialService: UserCredentialService) extends UserService{
  val log = Logger(this.getClass)

  override def create(user: User): Future[Either[ServiceError, User]] = {
    log.info("creating user: "+user.toString)
    userRepo
      .create(user)
      .map{
        case Right(user) => Right(user)
        case Left(error) => Left(SimpleServiceError)
      }
  }

  override def find(id: UUID): Future[Either[ServiceError, Option[User]]] = {
    userRepo
      .find(id)
      .map{
        case Right(user) => Right(user)
        case Left(error) => Left(SimpleServiceError)
      }
  }

  override def authenticate(email: String, password: String) = {
    log.info(s"authenticate user: $email")
    userCredentialService
      .find(email)
      .flatMap{
        case Right(Some(cred)) =>
          if(password.isBcrypted(cred.password)){
            find(cred.userId)
          }else{
            Future.successful(Right(None))
          }
        case Right(None) => Future.successful(Right(None))
        case Left(err) => Future.successful(Left(err))
      }
  }
}
