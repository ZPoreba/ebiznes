package models

import javax.inject.{ Inject, Singleton }
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import scala.concurrent.{ Future, ExecutionContext }

@Singleton
class ProductCategoryRepository @Inject() (val dbConfigProvider: DatabaseConfigProvider,
                                           val categoryRepository: CategoryRepository,
                                           val productRepository: ProductRepository)(implicit ec: ExecutionContext) {
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
  import categoryRepository.CategoryTable

  private val joinProductCategory = TableQuery[ProductTable] join TableQuery[ProductCategoryTable] on (_.id === _.productId)
  private val productCategory  = TableQuery[ProductCategoryTable]
  private val product = TableQuery[ProductTable]
  private val category = TableQuery[CategoryTable]

  def list(): Future[Seq[(Product, ProductCategory)]] = db.run {
    joinProductCategory.result
  }

  def getById(id: Long): Future[Option[Product]] = db.run {
      product.filter(_.id === id).result.headOption
    }


  // Create one product and add to categories
  def create(name: String, description: String, price: Int, categories: Array[Long]): Future[Object] = {
    val prodId = db.run {
        (product.map(p => (p.name, p.description, p.price))
          returning product.map(_.id)
          into {case ((name,description, price),id) => {
            Product(id, name, description, price)
          }}
          ) += (name, description, price)
      }
      .map(res => res.id)

    prodId.map(id => {
      for (catId <- categories) {
        val pc = ProductCategory(id, catId)
        db.run(productCategory += pc)
      }
    }).map(_ => prodId)
      .recover {
        case ex: Exception => ex.getCause.getMessage
      }
  }

  // Update one prodoct and add to categories if defined
  def update(id: Long, new_product: Product, categories: Array[Long]): Future[Unit] = {
    val productToUpdate: Product = new_product.copy(id)
    for (catId <- categories) {
      val pc = ProductCategory(id, catId)
      db.run(productCategory += pc)
    }
    db.run(product.filter(_.id === id).update(productToUpdate)).map(_ => ())
  }

  // Delete product and remove it from categories
  def deleteProduct(id: Long): Future[Unit] = {
    db.run(productCategory.filter(_.productId === id).delete).map(_ => ())
    db.run(product.filter(_.id === id).delete).map(_ => ())
  }

  def deleteCategory(id: Long): Future[Unit] = {
    db.run(productCategory.filter(_.categoryId === id).delete).map(_ => ())
    db.run(category.filter(_.id === id).delete).map(_ => ())
  }

  def deleteProductCategories(id: Long): Future[Unit] = {
    db.run(productCategory.filter(_.productId === id).delete).map(_ => ())
  }

}

