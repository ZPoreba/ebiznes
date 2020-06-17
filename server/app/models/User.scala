package models
import java.util.UUID
import com.mohiva.play.silhouette.api.{ Identity, LoginInfo }
import play.api.libs.json.Json

case class User(
  id: UUID,
  loginInfo: LoginInfo,
  firstName: Option[String],
  lastName: Option[String],
  fullName: Option[String],
  email: Option[String],
  avatarURL: Option[String],
  address: String) extends Identity

object User {
  implicit val userFormat = Json.format[User]
}
