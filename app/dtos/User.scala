package dtos

import java.sql.Timestamp
import java.util.{Calendar, UUID}

import com.github.t3hnar.bcrypt._
import pdi.jwt.{JwtAlgorithm, JwtJson}
import models.{User, UserCredential}
import play.api.libs.json._

case class SignupUser(email: String, firstname: String, lastname: String, password: String)
case class ResponseUser(id: UUID, email: String, firstname: String, lastname: String, created_date: Timestamp)
case class LoginUser(email: String, password: String)

object SignupUser {

  implicit class SignupUserOps(signupUser: SignupUser) {

    def toModel: (User, UserCredential) = {
      val userId = UUID.randomUUID
      val now = new Timestamp(Calendar.getInstance.getTime.getTime)
      val user = User(userId, signupUser.email, signupUser.firstname, signupUser.lastname, now)
      val userCredentail = UserCredential(signupUser.email, userId, signupUser.password)

      (user, userCredentail)
    }
  }

  implicit val readSignupUser: Reads[SignupUser] = Reads[SignupUser] {
    json =>
      for {
        email <- (json \ "email").validate[String]
          .filter(JsError("Email must not be empty"))(_.size > 0)
        firstname <- (json \ "firstname").validate[String]
          .filter(JsError("First name must not be empty"))(_.size > 0)
        lastname <- (json \ "lastname").validate[String]
          .filter(JsError("Last name must not be empty"))(_.size > 0)
        password <- (json \ "password").validate[String]
          .filter(JsError("Password must not be empty"))(_.size > 0)
      } yield SignupUser(email, firstname, lastname, password)
  }
}

object ResponseUser {
  implicit val writeResponseUser: Writes[ResponseUser] = Json.writes[ResponseUser]

  implicit class ResponseUserOps(responseUser: ResponseUser) {
    def toJson = Json.toJson(responseUser)(writeResponseUser)
  }
}

object LoginUser {
  implicit val readLoginUser: Reads[LoginUser] = Reads[LoginUser] {
    json =>
      for {
        email <- (json \ "email").validate[String]
          .filter(JsError("Email must not be empty"))(_.size > 0)
        password <- (json \ "password").validate[String]
          .filter(JsError("Password must not be empty"))(_.size > 0)
      } yield LoginUser(email, password)
  }
}