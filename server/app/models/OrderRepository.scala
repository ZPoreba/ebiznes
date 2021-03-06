package models

import javax.inject.{ Inject, Singleton }
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class OrderRepository @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  class OrderTable(tag: Tag) extends Table[Order](tag, "order_t") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def userId = column[String]("userId")
    def paymentId = column[Long]("paymentId")
    def status = column[String]("status")
    def * = (id, userId, paymentId, status) <> ((Order.apply _).tupled, Order.unapply)

  }

  private val order = TableQuery[OrderTable]

  def create(userId: String, paymentId: Long, status: String): Future[Long] = {
    db.run {
      (order.map(p => (p.userId, p.paymentId, p.status))
        returning order.map(_.id)
        into {
          case ((userId, paymentId, status), id) => {
            Order(id, userId, paymentId, status)
          }
        }) += (userId, paymentId, status)
    }
      .map(res => res.id)
  }

  def list(): Future[Seq[Order]] = db.run {
    order.result
  }

  def getById(id: Long): Future[Order] = db.run {
    order.filter(_.id === id).result.head
  }

  def getByIdOption(id: Long): Future[Option[Order]] = db.run {
    order.filter(_.id === id).result.headOption
  }

  def delete(id: Long): Future[Unit] = db.run(order.filter(_.id === id).delete).map(_ => ())

  def deleteUser(id: String): Future[Unit] = {
    db.run(order.filter(_.userId === id).delete).map(_ => ())
  }

  def update(id: Long, new_order: Order): Future[Unit] = {
    val orderToUpdate: Order = new_order.copy(id)
    db.run(order.filter(_.id === id).update(orderToUpdate)).map(_ => ())
  }

  def exists(id: Long): Future[Boolean] =
    db.run(order.filter(i => i.id === id).exists.result)

}
