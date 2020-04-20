package models

import play.api.libs.json._

case class WishListProduct(userId: Long, productId: Long)

object WishListProduct {
  implicit val wishListFormat = Json.format[WishListProduct]
}

