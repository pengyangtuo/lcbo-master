package service

import java.util.UUID

import models.UserCredential
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterEach, FunSpec, MustMatchers}
import repositories.{RepositoryError, UserCredentialRepo}
import services.ServiceError
import services.interpretor.UserCredentialServiceImpl
import com.github.t3hnar.bcrypt._

import scala.concurrent.Future

/**
  * Created by ypeng on 2017-04-20.
  */
class MockCredentialRepo extends UserCredentialRepo {

  var userCredentials: Map[String, UserCredential] = Map.empty[String, UserCredential]

  def flush = {
    userCredentials = userCredentials.empty
  }

  override def create(userCred: UserCredential): Future[Either[RepositoryError, UserCredential]] = {
    userCredentials = userCredentials + ((userCred.email, userCred))
    Future.successful(Right(userCred))
  }

  override def find(email: String): Future[Either[RepositoryError, Option[UserCredential]]] = {
    Future.successful(Right(userCredentials.get(email)))
  }

  override def update(userCred: UserCredential): Future[Either[RepositoryError, UserCredential]] = {
    userCredentials = userCredentials + ((userCred.email, userCred))
    Future.successful(Right(userCred))
  }
}

class UserCredentailImplSpec extends FunSpec with MustMatchers with ScalaFutures with BeforeAndAfterEach {

  val mockRepo = new MockCredentialRepo
  val userCredentialService = new UserCredentialServiceImpl(mockRepo)

  override def beforeEach = {
    mockRepo.flush
  }

  describe("UserCredentialServiceImpl") {

    val mockEmail = "mock@email.com"
    val mockId = UUID.randomUUID()
    val mockPassword = "mockme"

    def isSameCredential(encrypted: UserCredential, unEncrypted: UserCredential): Boolean = {
      return encrypted.email == unEncrypted.email &&
        encrypted.userId == unEncrypted.userId &&
        unEncrypted.password.isBcrypted(encrypted.password)
    }

    def assertUpdateSuccess(updateFuture: Future[Either[ServiceError, UserCredential]]) =
      whenReady(updateFuture) { result => result.isRight mustBe true }

    def assertFindSuccess(findFuture: Future[Either[ServiceError, Option[UserCredential]]], target: UserCredential) =
      whenReady(findFuture) { result => {
        result.isRight mustBe true
        result match {
          case Right(Some(cred)) => isSameCredential(cred, target) mustBe true
          case _ => assert(false)
        }
      }
      }

    def assertFindFailure(findFuture: Future[Either[ServiceError, Option[UserCredential]]], target: UserCredential) =
      whenReady(findFuture) { result => result mustBe Right(None) }

    describe("create()") {
      it("should create new user credential") {
        val cred = UserCredential(mockEmail, mockId, mockPassword)
        assertUpdateSuccess(userCredentialService.create(cred))
        assertFindSuccess(userCredentialService.find(cred.email), cred)
      }
    }

    describe("find()") {
      it("should return Right(Some(userCred)) if user credential exist, return Right(None) otherwise") {
        val cred = UserCredential(mockEmail, mockId, mockPassword)
        assertFindFailure(userCredentialService.find(cred.email), cred)
        assertUpdateSuccess(userCredentialService.create(cred))
        assertFindSuccess(userCredentialService.find(cred.email), cred)
      }
    }

    describe("update()") {
      it("should update existing user credential") {
        val cred = UserCredential(mockEmail, mockId, mockPassword)
        assertUpdateSuccess(userCredentialService.create(cred))
        assertFindSuccess(userCredentialService.find(cred.email), cred)

        val newCred = cred.copy(password = "newPassword")
        assertUpdateSuccess(userCredentialService.create(newCred))
        assertFindSuccess(userCredentialService.find(cred.email), newCred)
      }
    }

    describe("reset()") {
      it("should return Right(None) if input email does not exist") {
        val cred = UserCredential(mockEmail, mockId, mockPassword)

        whenReady(userCredentialService.reset(cred.email)) { result =>
          result mustBe Right(None)
        }
      }

      it("should update a user credential with a random password if it exist") {
        val cred = UserCredential(mockEmail, mockId, mockPassword)
        assertUpdateSuccess(userCredentialService.create(cred))
        assertFindSuccess(userCredentialService.find(cred.email), cred)

        var newPassword: String = ""

        whenReady(userCredentialService.reset(cred.email)) { result =>
          result match {
            case Right(Some(password)) => newPassword = password
            case _ => // do nothing
          }
        }
        val newCred = cred.copy(password = newPassword)
        assertFindSuccess(userCredentialService.find(cred.email), newCred)
      }
    }
  }
}
