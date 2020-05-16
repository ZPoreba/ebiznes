package models

import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CartProductRepository @Inject() (val dbConfigProvider: DatabaseConfigProvider,
                                       val userRepository: UserRepository,
                                       val productRepository: ProductRepository)(implicit ec: ExecutionContext) {

  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  class CartProductTable(tag: Tag) extends Table[CartProduct](tag, "cart_product") {
    def userId = column[Long]("userId")
    def productId = column[Long]("productId")
    def * = (userId, productId) <> ((CartProduct.apply _).tupled, CartProduct.unapply)

    def pk = primaryKey("primaryKey", (userId, productId))

    import userRepository.UserTable
    import productRepository.ProductTable

    def userFK = foreignKey("FK_USER", userId, TableQuery[UserTable])(user =>
      user.id, onDelete=ForeignKeyAction.Cascade)

    def productFK = foreignKey("FK_PRODUCT", productId, TableQuery[ProductTable])(product =>
      product.id, onDelete=ForeignKeyAction.Cascade)

  }

  import userRepository.UserTable

  val cartProduct = TableQuery[CartProductTable]
  val joinCartProduct = TableQuery[UserTable] join TableQuery[CartProductTable] on (_.id === _.userId)

  def deleteProduct(id: Long): Future[Unit] = {
    db.run(cartProduct.filter(_.productId === id).delete).map(_ => ())
  }

  def deleteUser(id: Long): Future[Unit] = {
    db.run(cartProduct.filter(_.userId === id).delete).map(_ => ())
  }

  def create(userId: Long, productId: Long): Unit = {
    val pc = CartProduct(userId, productId)
    db.run(cartProduct += pc)
  }

  def list(): Future[Seq[(Long, Long)]] = db.run {
    joinCartProduct.map{ case (u, p) => (u.id, p.productId) }.result
  }

  def getByUserId(id: Long): Future[Seq[(Long, Long)]] = db.run {
    joinCartProduct.map{ case (u, p) => (u.id, p.productId) }.filter(_._1 === id).result
  }

}

