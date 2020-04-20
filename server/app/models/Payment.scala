package models

import play.api.libs.json._

case class Payment(id: Long, date: java.sql.Date, status: String)

object Payment {
  implicit val paymentFormat = Json.format[Payment]
}

