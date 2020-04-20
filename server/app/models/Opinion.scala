package models

import play.api.libs.json._

case class Opinion(id: Long, userId: Long, productId: Long, content: String)

object Opinion {
  implicit val opinionFormat = Json.format[Opinion]
}

