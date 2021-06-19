package models
import java.util.UUID

import com.mohiva.play.silhouette.api.Identity
import play.api.libs.json.Json

case class ApiUser(
  id: String,
  firstName: Option[String],
  lastName: Option[String],
  fullName: Option[String],
  email: Option[String],
  avatarURL: Option[String],
  address: String) extends Identity

object ApiUser {
  implicit val userFormat = Json.format[ApiUser]
}
