package models

import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DiscountCodeRepository @Inject() (val dbConfigProvider: DatabaseConfigProvider,
                                        val productRepository: ProductRepository
                                       )(implicit ec: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  class DiscountCodeTable(tag: Tag) extends Table[DiscountCode](tag, "discount_code") {

    def productId: Rep[Long] = column[Long]("productId")
    def code: Rep[Long] = column[Long]("code")

    def * = (productId, code) <> ((DiscountCode.apply _).tupled, DiscountCode.unapply)
    def pk = primaryKey("primaryKey", (productId, code))

    import productRepository.ProductTable

    def productFK = foreignKey("FK_PRODUCT", productId, TableQuery[ProductTable])(product =>
      product.id, onDelete=ForeignKeyAction.Cascade)

  }

  val discountCode = TableQuery[DiscountCodeTable]

  def create(productId: Long, code: Long): Unit = {
    val dc = DiscountCode(productId, code)
    db.run(discountCode += dc)
  }

  def list(): Future[Seq[DiscountCode]] = db.run {
    discountCode.result
  }

  // get by id of product
  def getById(id: Long): Future[Option[DiscountCode]] = db.run {
    discountCode.filter(_.productId === id).result.headOption
  }

  def delete(code: Long): Future[Unit] = db.run(discountCode.filter(_.code === code).delete).map(_ => ())

}

