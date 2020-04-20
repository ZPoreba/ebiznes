package models

import javax.inject.{ Inject, Singleton }
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import scala.concurrent.{ Future, ExecutionContext }

@Singleton
class ProductRepository @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._


  class ProductTable(tag: Tag) extends Table[Product](tag, "product") {


    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def description = column[String]("description")
    def price = column[Int]("price")
    def * = (id, name, description, price) <> ((Product.apply _).tupled, Product.unapply)

  }

  private val product = TableQuery[ProductTable]

  def create(name: String, description: String, price: Int): Future[Long] = {
    db.run {
      (product.map(p => (p.name, p.description, p.price))
        returning product.map(_.id)
        into {case ((name,description, price),id) => {
        Product(id, name, description, price)
      }}
        ) += (name, description, price)
    }
      .map(res => res.id)
  }

  def list(): Future[Seq[Product]] = db.run {
    product.result
  }

  def getById(id: Long): Future[Option[Product]] = db.run {
    product.filter(_.id === id).result.headOption
  }

  def delete(id: Long): Future[Unit] = db.run(product.filter(_.id === id).delete).map(_ => ())

  def update(id: Long, new_product: Product): Future[Unit] = {
    val productToUpdate: Product = new_product.copy(id)
    db.run(product.filter(_.id === id).update(productToUpdate)).map(_ => ())
  }

  def exists(id: Long): Future[Boolean] =
    db.run(product.filter(i => i.id === id).exists.result)

}

