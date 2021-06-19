package models

import play.api.libs.json._

case class Return(id: Long, userId: String, productId: Long, status: String)

object Return {
  implicit val returnFormat = Json.format[Return]
}

