package models

import play.api.libs.json.Json

case class ProductCategory(productId: Long, categoryId: Long)

object ProductCategory {
  implicit val productCategoryFormat = Json.format[ProductCategory]
}
