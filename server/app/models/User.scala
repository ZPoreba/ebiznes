package models

import play.api.libs.json._

case class User(id: Long, firstName: String, secondName: String, email: String, password: String, address: String)

// Cart and WishList are connected by user id

object User {
  implicit val userFormat = Json.format[User]
}

