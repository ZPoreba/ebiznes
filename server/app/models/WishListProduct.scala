package models

import play.api.libs.json._

case class WishListProduct(userId: String, productId: Long)

object WishListProduct {
  implicit val wishListFormat = Json.format[WishListProduct]
}

