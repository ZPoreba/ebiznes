package models

import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CategoryRepository @Inject() (val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  class CategoryTable(tag: Tag) extends Table[Category](tag, "category") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def * = (id, name) <> ((Category.apply _).tupled, Category.unapply)
  }

//  import productRepository.ProductCategoryTable

  val category = TableQuery[CategoryTable]
//  val productCategory = TableQuery[ProductCategoryTable]

  def create(name: String): Future[Category] = db.run {
    (category.map(c => (c.name))
      returning category.map(_.id)
      into ((name, id) => Category(id, name))
      ) += (name)
  }

  def list(): Future[Seq[Category]] = db.run {
    category.result
  }

  def getById(id: Long): Future[Option[Category]] = db.run {
    category.filter(_.id === id).result.headOption
  }

  def update(id: Long, new_category: Category): Future[Unit] = {
    val categoryToUpdate: Category = new_category.copy(id)
    db.run(category.filter(_.id === id).update(categoryToUpdate)).map(_ => ())
  }

}

