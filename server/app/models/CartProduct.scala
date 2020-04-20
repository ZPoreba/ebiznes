package models

import play.api.libs.json._

case class CartProduct(userId: Long, productId: Long)

object CartProduct {
  implicit val cartProductFormat = Json.format[CartProduct]
}

