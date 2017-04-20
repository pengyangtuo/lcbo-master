import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport
import repositories.{UserCredentialRepo, UserRepo}
import repositories.interpretor.{UserCredentialRepoImpl, UserRepoImpl}
import services.{UserCredentialService, UserService}
import services.interpretor.{UserCredentialServiceImpl, UserServiceImpl}

/**
  * Created by ypeng on 2017-04-16.
  */
class Module extends AbstractModule with AkkaGuiceSupport{
  override def configure() = {
    bind(classOf[UserService])
      .to(classOf[UserServiceImpl])
      .asEagerSingleton()

    bind(classOf[UserRepo])
      .to(classOf[UserRepoImpl])
      .asEagerSingleton()

    bind(classOf[UserCredentialService])
      .to(classOf[UserCredentialServiceImpl])
      .asEagerSingleton()

    bind(classOf[UserCredentialRepo])
      .to(classOf[UserCredentialRepoImpl])
      .asEagerSingleton()
  }
}
