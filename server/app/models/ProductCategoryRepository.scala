package models

import javax.inject.{ Inject, Singleton }
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import scala.concurrent.{ Future, ExecutionContext }

@Singleton
class ProductCategoryRepository @Inject() (val dbConfigProvider: DatabaseConfigProvider,
                                           val categoryRepository: CategoryRepository,
                                           val productRepository: ProductRepository
                                          )(implicit ec: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._


  class ProductCategoryTable(tag: Tag) extends Table[ProductCategory](tag, "product_category") {

    def productId: Rep[Long] = column[Long]("productId")
    def categoryId: Rep[Long] = column[Long]("categoryId")

    def * = (productId, categoryId) <> ((ProductCategory.apply _).tupled, ProductCategory.unapply)
    def pk = primaryKey("primaryKey", (productId, categoryId))

    import productRepository.ProductTable
    import categoryRepository.CategoryTable

    def productFK = foreignKey("FK_PRODUCT", productId, TableQuery[ProductTable])(product =>
    product.id, onDelete=ForeignKeyAction.Cascade)

    def categoryFK = foreignKey("FK_CATEGORY", categoryId, TableQuery[CategoryTable])(category =>
      category.id, onDelete=ForeignKeyAction.Cascade)

  }

  import productRepository.ProductTable

  private val joinProductCategory = TableQuery[ProductTable] join TableQuery[ProductCategoryTable] on (_.id === _.productId)
  private val productCategory  = TableQuery[ProductCategoryTable]

  def list(): Future[Seq[(Long, String, String, Long)]] = db.run {
    joinProductCategory.map{ case (p, a) => (p.id, p.name, p.description, a.categoryId) }.result
  }

  def getByProductId(id: Long): Future[Seq[ProductCategory]] = db.run {
    productCategory.filter(_.productId === id).result
  }

  def getByCategoryId(id: Long): Future[Seq[ProductCategory]] = db.run {
    productCategory.filter(_.categoryId === id).result
  }

  def create(productId: Long, categoryId: Long): Unit = {
    val pc = ProductCategory(productId, categoryId)
    db.run(productCategory += pc)
  }

  def deleteProduct(id: Long): Future[Unit] = db.run(productCategory.filter(_.productId === id).delete).map(_ => ())
  def deleteCategory(id: Long): Future[Unit] = db.run(productCategory.filter(_.categoryId === id).delete).map(_ => ())

}

