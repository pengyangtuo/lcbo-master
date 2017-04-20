package controllers

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.google.inject.Inject
import configuration.Settings
import play.api._
import play.api.mvc._
import play.api.libs.ws._
import play.api.http.HttpEntity

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by ypeng on 2017-04-18.
  */
class LcboController @Inject()(config: Configuration, ws: WSClient, implicit val system: ActorSystem)
  extends Controller with Secured with RequestLogging {

  implicit val materializer = ActorMaterializer()

  val settings = Settings(config)
  val jwtSigningSecret = settings.jwt.secret
  val lcboHost = settings.lcbo.host
  val lcboApiKey = settings.lcbo.key

  def locator(query: Option[String]) = Action.async { implicit request =>
    logIncomingRequest(s"/locator?${query.getOrElse("")}")
    validateJwtToken(request) match {
      case None => Future.successful(Unauthorized)
      case Some(uuid) =>
        val wsRequest = ws
          .url(s"$lcboHost/stores")
          .withHeaders("Authorization" -> s"Token $lcboApiKey")
          .withRequestTimeout(10000.millis)
          .withQueryString("q" -> query.getOrElse(""))

        log.info(wsRequest.toString)

//        Future.successful(Ok)
        wsRequest.get().map{ response =>
          log.info("WS response")
          log.info(response.json.toString)
          Ok(response.body)
        }
    }
  }

}
