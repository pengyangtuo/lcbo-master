package models

import java.sql.Timestamp
import java.util.UUID

import dtos.ResponseUser

case class User(id: UUID, email: String, firstname: String, lastname: String, created_date: Timestamp)

object User{
  implicit class UserOps(user: User) {
    def toDto: ResponseUser = ResponseUser(user.id, user.email, user.firstname, user.lastname, user.created_date)
  }
}