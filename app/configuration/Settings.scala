package configuration

import play.api.Configuration

/**
  * Created by ypeng on 2017-04-16.
  */
case class Settings(config: Configuration) {
  object cassandra {
    val endpoint = config.getString("cassandra.endpoint").getOrElse("127.0.0.1")
    val port = config.getInt("cassandra.port").getOrElse(9042)
    val keyspace = config.getString("cassandra.keyspace").getOrElse("lcbo_master")

    val userTable = config.getString("cassandra.tables.users").getOrElse("users")
    val userCredentialTable = config.getString("cassandra.tables.usercredentials").getOrElse("usercredentials")
  }

  object jwt {
    val secret = config.getString("jwt.secret").getOrElse("secret")
  }

  object mailOffice{
    val email = config.getString("mail-office.email").getOrElse("")
    val key = config.getString("mail-office.key").getOrElse("")
    val resetPasswordClient = config.getString("mail-office.reset-client").getOrElse("")
  }

  object lcbo{
    val host = config.getString("lcbo.host").getOrElse("")
    val key = config.getString("lcbo.key").getOrElse("")
  }
}
