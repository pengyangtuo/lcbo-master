package services

/**
  * Created by ypeng on 2017-04-16.
  */
trait ServiceError
case object SimpleServiceError extends ServiceError
case object UnsupportedService extends ServiceError
