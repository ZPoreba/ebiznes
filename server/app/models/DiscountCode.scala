package models

import play.api.libs.json._

case class DiscountCode(productId: Long, code: Long)

object DiscountCode {
  implicit val discountCodeFormat = Json.format[DiscountCode]
}

