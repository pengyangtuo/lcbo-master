package controllers

import java.util.UUID

import pdi.jwt.{JwtAlgorithm, JwtJson}
import play.api.Logger
import play.api.libs.json._
import play.api.mvc.Request

import scala.util.{Failure, Success, Try}

/**
  * Created by ypeng on 2017-04-16.
  */
trait Secured extends RequestLogging {
  val jwtSigningSecret: String

  def createJwtToken(json: JsValue) = {
    val claim = json.as[JsObject] + ("issuer" -> JsString("lcbo-master"))

    val token = JwtJson.encode(claim, jwtSigningSecret, JwtAlgorithm.HS256)

    Json.obj(("accessToken", token))
  }

  def validateJwtToken(request: Request[_]): Option[UUID] = {
    val authHeaderOption = request.headers.get("Authorization")

    authHeaderOption match {
      case Some(authHeader) =>
        val token = authHeader.split(" ").last
        val decodedToken: Try[JsObject] = JwtJson.decodeJson(token, jwtSigningSecret, Seq(JwtAlgorithm.HS256))

        decodedToken match {
          case Success(tokenJson) =>
            val uuidOpt = tokenJson.value.get("id").map(tokenString => {
              UUID.fromString(tokenString.as[JsString].value)
            })
            log.info(s" * id extracted from auth token: $uuidOpt")
            uuidOpt
          case Failure(_) =>
            log.info(s" * invalid auth token found in secured request")
            None
        }
      case None =>
        log.info(s" * no auth header set in a secured request")
        None
    }
  }
}
