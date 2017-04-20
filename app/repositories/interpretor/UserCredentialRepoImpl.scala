package repositories.interpretor

import java.util.UUID

import com.datastax.driver.core.{Cluster, ResultSet, Session}
import com.google.inject.Inject
import configuration.Settings
import models.{User, UserCredential}
import play.api.{Configuration, Logger}
import repositories.{RecoverableRepo, RepositoryError, UnavailableToConnect, UserCredentialRepo}
import troy.dsl._
import troy.driver.DSL._

import scala.concurrent.Future

/**
  * Created by ypeng on 2017-04-16.
  */
class UserCredentialRepoImpl @Inject()(config: Configuration) extends UserCredentialRepo with RecoverableRepo {
  import scala.concurrent.ExecutionContext.Implicits.global

  val log = Logger(this.getClass)
  val settings = Settings(config)
  val cassandraEndpoint = settings.cassandra.endpoint
  val cassandraPort = settings.cassandra.port
  val cassandraKeyspace = settings.cassandra.keyspace

  val userTable = settings.cassandra.userTable

  val cluster = Cluster.builder()
    .withPort(cassandraPort)
    .addContactPoint(cassandraEndpoint)
    .build()

  implicit val session: Session = cluster.connect(cassandraKeyspace)

  object query {
    val insertQuery = withSchema {
      (userCred: UserCredential) =>
        cql"""
          INSERT INTO lcbo_master.usercredentials (email, id, password)
          VALUES (${userCred.email}, ${userCred.userId}, ${userCred.password});
        """.prepared.executeAsync
    }

    val findQuery = withSchema {
      (email: String) =>
        cql"""
          SELECT * FROM lcbo_master.usercredentials
          WHERE email = $email
        """
          .prepared
          .executeAsync
    }

    val updateQuery = withSchema {
      (userCred: UserCredential) =>
        cql"""
          UPDATE lcbo_master.usercredentials
          SET password = ${userCred.password}
          WHERE email = ${userCred.email} IF EXISTS;
        """.prepared.executeAsync
    }
  }

  override def create(userCred: UserCredential) = {
    val queryRes = query.insertQuery(userCred)
      .map(result => {
        log.info(result.toString)
        Right(userCred)
      })

    recoverFromDatabaseError(queryRes)
  }

  override def find(email: String) = {
    val queryRes = query.findQuery(email)
      .map((resultSet: ResultSet) => {
        val rows = resultSet.all()
        log.info(s"found user credential (${rows.size}): ${rows.toString}")

        if(rows.isEmpty){
          Right(None)
        }else{
          val head = rows.get(0)
          val cred = UserCredential(head.getString("email"), head.getUUID("id"), head.getString("password"))
          Right(Some(cred))
        }
      })

    recoverFromDatabaseError(queryRes)
  }

  override def update(userCred: UserCredential): Future[Either[RepositoryError, UserCredential]] = {
    val queryRes = query.updateQuery(userCred)
      .map(result => {
        log.info(result.toString)
        Right(userCred)
      })

    recoverFromDatabaseError(queryRes)
  }
}
