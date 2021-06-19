package models

import javax.inject.{ Inject, Singleton }
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class OrderProductRepository @Inject() (
  val dbConfigProvider: DatabaseConfigProvider,
  val orderRepository: OrderRepository,
  val productRepository: ProductRepository)(implicit ec: ExecutionContext) {

  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  class OrderProductTable(tag: Tag) extends Table[OrderProduct](tag, "order_product") {
    def orderId = column[Long]("orderId")
    def productId = column[Long]("productId")
    def * = (orderId, productId) <> ((OrderProduct.apply _).tupled, OrderProduct.unapply)

    def pk = primaryKey("primaryKey", (orderId, productId))

    import orderRepository.OrderTable
    import productRepository.ProductTable

    def orderFK = foreignKey("FK_ORDER", orderId, TableQuery[OrderTable])(order =>
      order.id, onDelete = ForeignKeyAction.Cascade)

    def productFK = foreignKey("FK_PRODUCT", productId, TableQuery[ProductTable])(product =>
      product.id, onDelete = ForeignKeyAction.Cascade)

  }

  import orderRepository.OrderTable

  val orderProduct = TableQuery[OrderProductTable]
  val joinOrderProduct = TableQuery[OrderTable] join TableQuery[OrderProductTable] on (_.id === _.orderId)

  def deleteProduct(id: Long): Future[Unit] = {
    db.run(orderProduct.filter(_.productId === id).delete).map(_ => ())
  }

  def deleteOrder(id: Long): Future[Unit] = {
    db.run(orderProduct.filter(_.orderId === id).delete).map(_ => ())
  }

  def create(orderId: Long, productId: Long): Unit = {
    val op = OrderProduct(orderId, productId)
    db.run(orderProduct += op)
  }

  def list(): Future[Any] = db.run {
    joinOrderProduct.map { case (o, p) => (o.id, o.userId, o.paymentId, o.status, p.productId) }.result
  }

  def getByOrderId(id: Long): Future[Seq[(Long, String, Long, String, Long)]] = db.run {
    joinOrderProduct.map { case (o, p) => (o.id, o.userId, o.paymentId, o.status, p.productId) }.filter(_._1 === id).result
  }

  def getByUserId(userId: String): Future[Seq[(Long, String)]] = db.run {
    joinOrderProduct.map { case (o, p) => (o.id, o.userId) }.filter(_._2 === userId).result
  }

}
