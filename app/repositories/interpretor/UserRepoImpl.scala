package repositories.interpretor

import java.sql.Timestamp
import java.util.UUID

import com.datastax.driver.core.{Cluster, ResultSet, Session}
import com.google.common.util.concurrent.ListenableFuture
import com.google.inject.Inject
import configuration.Settings
import models.User
import play.api.{Configuration, Logger}
import repositories.{RecoverableRepo, RepositoryError, UnavailableToConnect, UserRepo}
import troy.dsl._
import troy.driver.DSL._

import scala.concurrent.Future

/**
  * Created by ypeng on 2017-04-16.
  */
class UserRepoImpl @Inject()(config: Configuration) extends UserRepo with RecoverableRepo {

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

  def create(user: User) = {
    val insertQuery = withSchema {
      (user: User) =>
        cql"""
          INSERT INTO lcbo_master.users (id, email, firstname, lastname, created_date)
          VALUES (${user.id}, ${user.email}, ${user.firstname}, ${user.lastname}, toTimestamp(now()));
        """.prepared.executeAsync
    }

    insertQuery(user)
      .map(result => {
        log.info(result.toString)
        Right(user)
      })
  }

  def find(id: UUID) = {
    val findQuery = withSchema {
      (id: UUID) =>
        cql"""
          SELECT * FROM lcbo_master.users
          WHERE id = ${id}
        """
          .prepared
          .executeAsync
    }

    val queryRes = findQuery(id)
      .map((resultSet: ResultSet) => {
        val rows = resultSet.all()
        log.info(s"found user (${rows.size}): ${rows.toString}")

        if(rows.isEmpty){
          Right(None)
        }else{
          val head = rows.get(0)
          val user = User(head.getUUID("id"), head.getString("email"),
            head.getString("firstname"),
            head.getString("lastname"), new Timestamp(head.getTimestamp("created_date").getTime))
          Right(Some(user))
        }
      })

    recoverFromDatabaseError(queryRes)
  }
}
