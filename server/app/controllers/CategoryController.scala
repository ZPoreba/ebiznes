package controllers

import javax.inject._
import models.{Category, CategoryRepository, ProductCategoryRepository}
import play.api.mvc._
import play.filters.csrf.CSRF
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CategoryController @Inject()(categoryRepository: CategoryRepository, productCategoryRepository: ProductCategoryRepository, cc: MessagesControllerComponents)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  def getToken = Action { implicit request =>
    val token = CSRF.getToken.get.value
    Ok(token)
  }

  def create = Action.async { implicit request =>
    val params = request.queryString.map { case (k,v) => k -> v.mkString }
    if (!params.contains("name")) {
      Future(Ok("No name parameter in query"))
    }
    else {
      categoryRepository.create(params("name"))
      Future(Ok("Category created!"))
    }
  }

  def read: Action[AnyContent] = Action.async { implicit request =>
    val categories = categoryRepository.list()
    categories.map( category => Ok(category.toString()) )
  }

  def readById: Action[AnyContent] = Action.async { implicit request =>

    val params = request.queryString.map { case (k,v) => k -> v.mkString }
    if (!params.contains("id")) {
      Future(Ok("No id parameter in query"))
    }
    else {
      val categories = categoryRepository.getById(params("id").toLong)
      categories.map(category => category match {
        case Some(c) => Ok(c.toString())
        case None => Ok("No product with id")
      })
    }

  }

  def update = Action.async { implicit request =>
    val params = request.queryString.map { case (k,v) => k -> v.mkString }

    if (!params.contains("id")) {
      Future(Ok("No id parameter in query"))
    }
    else {

      try {
        val id = params("id").toLong
        val categories = categoryRepository.getById(id)

        categories.map(category => category match {
          case Some(c) => {
            val name = if (params.contains("name")) params("name") else c.name

            val newCategory = Category(id, name)
            categoryRepository.update(id, newCategory)
            Ok("Category updated!")
          }
          case None => Ok("No object with such id")
        })
      }
      catch {
        case e: NumberFormatException => Future(Ok("Id has to be integer"))
      }

    }
  }

  def delete = Action { implicit request =>
    val params = request.queryString.map { case (k,v) => k -> v.mkString }

    if (!params.contains("id")) {
      Ok("No id parameter in query")
    }
    else {
      try {
        productCategoryRepository.deleteCategory(params("id").toLong)
        Ok("Category deleted!")
      }
      catch {
        case e: NumberFormatException => Ok("Id has to be integer")
      }
    }
  }

}
