package models

import play.api.libs.json._

case class CartProduct(userId: String, productId: Long)

object CartProduct {
  implicit val cartProductFormat = Json.format[CartProduct]
}

