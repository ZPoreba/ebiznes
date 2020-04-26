package models

import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WishListProductRepository @Inject() (val dbConfigProvider: DatabaseConfigProvider,
                                           val userRepository: UserRepository,
                                           val productRepository: ProductRepository)(implicit ec: ExecutionContext) {

  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  class WishListProductTable(tag: Tag) extends Table[WishListProduct](tag, "wishlist_product") {
    def userId = column[Long]("userId")
    def productId = column[Long]("productId")
    def * = (userId, productId) <> ((WishListProduct.apply _).tupled, WishListProduct.unapply)

    def pk = primaryKey("primaryKey", (userId, productId))

    import userRepository.UserTable
    import productRepository.ProductTable

    def userFK = foreignKey("FK_USER", userId, TableQuery[UserTable])(user =>
      user.id, onDelete=ForeignKeyAction.Cascade)

    def productFK = foreignKey("FK_PRODUCT", productId, TableQuery[ProductTable])(product =>
      product.id, onDelete=ForeignKeyAction.Cascade)

  }

  import userRepository.UserTable

  val wishListProduct = TableQuery[WishListProductTable]
  val joinWishListProduct = TableQuery[UserTable] join TableQuery[WishListProductTable] on (_.id === _.userId)

  def deleteProduct(id: Long): Future[Unit] = {
    db.run(wishListProduct.filter(_.productId === id).delete).map(_ => ())
  }

  def deleteUser(id: Long): Future[Unit] = {
    db.run(wishListProduct.filter(_.userId === id).delete).map(_ => ())
  }

  def create(userId: Long, productId: Long): Unit = {
    val pc = WishListProduct(userId, productId)
    db.run(wishListProduct += pc)
  }

  def list(): Future[Any] = db.run {
    joinWishListProduct.map{ case (u, p) => (u.id, p.productId) }.result
  }

  def getByUserId(id: Long): Future[Seq[(Long, Long)]] = db.run {
    joinWishListProduct.map{ case (u, p) => (u.id, p.productId) }.filter(_._1 === id).result
  }

}

