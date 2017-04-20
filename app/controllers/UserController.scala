package controllers

import javax.inject._

import actors.{MailMan, UserCreated, UserPasswordReset}
import akka.actor.ActorSystem
import configuration.Settings
import dtos.{ChangePassword, LoginUser, ResetPassword, SignupUser}
import models.User
import play.api._
import play.api.data.validation.ValidationError
import play.api.libs.json.{JsPath, JsValue}
import play.api.mvc._
import services.{ServiceError, UserCredentialService, UserService}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class UserController @Inject()(userService: UserService, userCredentialService: UserCredentialService, system: ActorSystem, config: Configuration)
  extends Controller with Secured with RequestLogging {

  val settings = Settings(config)
  val jwtSigningSecret = settings.jwt.secret

  createMailMan()

  def signup = Action.async(parse.json) { implicit request =>
    logIncomingRequest("/user/signup")
    request
      .body
      .validate[SignupUser]
      .asEither
      .fold(handleValidationError, createUser)
  }

  def login = Action.async(parse.json) { request =>
    logIncomingRequest("/user/login")
    request
      .body
      .validate[LoginUser]
      .asEither
      .fold(handleValidationError, loginUser)
  }

  def changePassword = Action.async(parse.json) { request =>
    logIncomingRequest("/user/changepassword")
    request
      .body
      .validate[ChangePassword]
      .asEither
      .fold(handleValidationError, chanageUserPassword)
  }

  def resetPassword = Action.async(parse.json) { request =>
    logIncomingRequest("/user/resetpassword")
    request
      .body
      .validate[ResetPassword]
      .asEither
      .fold(handleValidationError, resetUserPassword)
  }


  def get = Action.async { request =>
    logIncomingRequest("/user")
    validateJwtToken(request) match {
      case None => Future.successful(Unauthorized)
      case Some(uuid) =>
        userService
          .find(uuid)
          .map {
            case Right(Some(user)) => Ok(user.toDto.toJson)
            case Right(None) => NotFound
            case Left(_) => ServiceUnavailable
          }
    }
  }

  def chanageUserPassword(changePassword: ChangePassword) = {
    userCredentialService
      .update(changePassword.email, changePassword.oldPassword, changePassword.newPassword)
      .map {
        case Right(None) =>
          NotFound
        case Right(Some(Success(cred))) =>
          Ok
        case Right(Some(Failure(throwable))) =>
          BadRequest(throwable.getMessage)
        case Left(serviceError) =>
          ServiceUnavailable
      }
  }

  def resetUserPassword(resetPassword: ResetPassword) = {
    userCredentialService
      .reset(resetPassword.email)
      .map {
        case Right(None) =>
          NotFound
        case Right(Some(plainTextPassword)) =>
          system.eventStream.publish(UserPasswordReset(resetPassword.email, plainTextPassword))
          Ok
        case Left(serviceError) =>
          ServiceUnavailable
      }
  }

  def createUser(signupUser: SignupUser) = {
    userCredentialService
      .find(signupUser.email)
      .flatMap {
        case Right(None) =>
          val (user, userCred) = signupUser.toModel
          val createResult = for {
            userCred <- userCredentialService.create(userCred)
            user <- userService.create(user)
          } yield user

          createResult.map {
            case Right(user) =>
              system.eventStream.publish(UserCreated(user.email, user.firstname))
              Created(user.toDto.toJson)
            case Left(serviceError) => ServiceUnavailable
          }

        case Right(_) =>
          Future.successful(Conflict)
        case Left(serviceError) =>
          Future.successful(ServiceUnavailable)
      }
  }

  def loginUser(loginUser: LoginUser) = {
    userService
      .authenticate(loginUser.email, loginUser.password)
      .map {
        case Right(Some(user)) => Ok(createJwtToken(user.toDto.toJson))
        case Right(None) => Unauthorized
        case Left(_) => ServiceUnavailable
      }
  }

  def handleValidationError(error: Seq[(JsPath, Seq[ValidationError])]) = {
    val msg: Seq[String] = error.map {
      case (jsPath, validationErrors) =>
        validationErrors.map(_.message).mkString(
          start = jsPath.path.mkString(start = "", sep = "/", end = ""),
          sep = ",",
          end = ""
        )
    }
    Future.successful(BadRequest(msg.toString))
  }

  def createMailMan(): Unit = {
    val mailOffice = settings.mailOffice.email
    val mailOfficeKey = settings.mailOffice.key
    val resetClient = settings.mailOffice.resetPasswordClient

    val mailMan = system.actorOf(MailMan.props(mailOffice, mailOfficeKey, resetClient), "mail-man")
  }
}
