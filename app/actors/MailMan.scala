package actors

import akka.actor.{Actor, ActorLogging, Props}
import courier._
import Defaults._

import scala.util.{Failure, Success}

/**
  * Created by ypeng on 2017-04-18.
  */
class MailMan(mailOffice: String, mailOfficeKey: String, resetPasswordClient: String) extends Actor with ActorLogging {

  val mailer = Mailer("smtp.gmail.com", 587)
    .auth(true)
    .as(mailOffice, mailOfficeKey)
    .startTtls(true)()

  override def preStart(): Unit = {
    log.info("created MailMan actor: " + self.path)
    context.system.eventStream.subscribe(context.self, classOf[SystemMessage])
  }

  override def postStop() = {
    context.system.eventStream.unsubscribe(context.self)
  }

  override def receive = {
    case UserCreated(email, firstname) =>
      val title = "Welcome to LCBO Master"
      val htmlBody = s"<html><body><h1>Welcome, $firstname!</h1></body></html>"
      sendMail(email, title, htmlBody)
        .onComplete{
          case Success(_) => log.info(s"sent welcome email to new user ($email)")
          case Failure(_) => log.warning(s"unable to send welcome email to user ($email)")
        }

    case UserPasswordReset(email, newPassword) =>
      val title = "Reset password to LCBO Master"
      val htmlBody =
        s"""
          |<html><body>
          |<h1>Click the following link to reset your password:</h1><br>
          |<a href="${resetPasswordClient}?oldPassword=$newPassword&email=$email">Reset password</a>
          |</body></html>""".stripMargin
      sendMail(email, title, htmlBody)
        .onComplete{
          case Success(_) => log.info(s"sent reset password email to user ($email)")
          case Failure(_) => log.warning(s"unable to send reset password email to user ($email)")
        }

    case _ => log.info("unhandled message in MailMan") // do nothing
  }

  def sendMail(email: String, title: String, htmlBody: String) = {
    mailer(Envelope.from(mailOffice.addr)
      .to(email.addr)
      .subject(title)
      .content(
        Multipart().html(htmlBody)
      ))
  }
}

object MailMan {
  def props(mailOffice: String, mailOfficeKey: String, resetClient: String) = Props(new MailMan(mailOffice, mailOfficeKey, resetClient))
}
