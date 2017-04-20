package actors

/**
  * Created by ypeng on 2017-04-18.
  */
trait SystemMessage
case class UserCreated(email: String, firstname: String) extends SystemMessage
case class UserPasswordReset(email: String, plainTextPassword: String) extends SystemMessage
