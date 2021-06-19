package models

import play.api.libs.json.Json

case class OrderProduct(orderId: Long, productId: Long)

object OrderProduct {
  implicit val orderProductFormat = Json.format[OrderProduct]
}
