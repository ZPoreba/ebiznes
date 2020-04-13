package models

import play.api.libs.json.Json

case class ProductCategory(productId: Long, categoryId: Long)

object ProductCategory {
  implicit val productFormat = Json.format[Product]
}
