package models

import play.api.libs.json._

case class Order(id: Long, userId: Long, paymentId: Long, status: String)

object Order {
  implicit val orderFormat = Json.format[Order]
}

