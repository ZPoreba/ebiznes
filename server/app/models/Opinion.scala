package models

import play.api.libs.json._

case class Opinion(id: Long, userId: String, productId: Long, content: String)

object Opinion {
  implicit val opinionFormat = Json.format[Opinion]
}

