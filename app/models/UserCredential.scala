package models
import java.util.UUID

case class UserCredential(email: String, userId: UUID, password: String)