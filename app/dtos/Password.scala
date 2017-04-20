package dtos

import models.UserCredential
import play.api.libs.json.{JsError, Reads}

/**
  * Created by ypeng on 2017-04-18.
  */
case class ChangePassword(email: String, oldPassword: String, newPassword: String)
case class ResetPassword(email: String)

object ChangePassword {

  implicit val readChangePassword: Reads[ChangePassword] = Reads[ChangePassword] {
    json =>
      for {
        email <- (json \ "email").validate[String]
          .filter(JsError("Email must not be empty"))(_.size > 0)
        oldPassword <- (json \ "oldPassword").validate[String]
          .filter(JsError("oldPassword must not be empty"))(_.size > 0)
        newPassword <- (json \ "newPassword").validate[String]
          .filter(JsError("newPassword must not be empty"))(_.size > 0)
      } yield ChangePassword(email, oldPassword, newPassword)
  }
}

object ResetPassword {

  implicit val readResetPassword: Reads[ResetPassword] = Reads[ResetPassword] {
    json =>
      for {
        email <- (json \ "email").validate[String]
          .filter(JsError("Email must not be empty"))(_.size > 0)
      } yield ResetPassword(email)
  }
}


