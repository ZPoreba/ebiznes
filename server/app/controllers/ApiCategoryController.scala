package controllers

import javax.inject._
import models.{ Category, CategoryRepository, ProductCategoryRepository }
import play.api.libs.json._
import play.api.mvc._
import play.filters.csrf.CSRF
import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, ExecutionContext, Future }

@Singleton
class ApiCategoryController @Inject() (scc: SilhouetteControllerComponents, categoryRepository: CategoryRepository, productCategoryRepository: ProductCategoryRepository)(implicit ec: ExecutionContext) extends SilhouetteController(scc) {

  def getToken = Action { implicit request =>
    val token = CSRF.getToken.get.value
    Ok(token)
  }

  def create = SecuredAction.async { implicit request =>
    val params = request.queryString.map { case (k, v) => k -> v.mkString }
    if (!params.contains("name")) {
      Future(Ok("No name parameter in query"))
    } else {
      categoryRepository.create(params("name"))
      Future(Ok("Category created!"))
    }
  }

  def read: Action[AnyContent] = SecuredAction { implicit request =>
    val categories = Await.result(categoryRepository.list(), Duration.Inf)
    Ok(Json.toJson(categories))
  }

  def readById: Action[AnyContent] = SecuredAction.async { implicit request =>

    val params = request.queryString.map { case (k, v) => k -> v.mkString }
    if (!params.contains("id")) {
      Future(Ok("No id parameter in query"))
    } else {
      val categories = categoryRepository.getByIdOption(params("id").toLong)
      categories.map(category => category match {
        case Some(c) => Ok(Json.toJson(c))
        case None => Ok("No product with id")
      })
    }

  }

  def update = SecuredAction.async { implicit request =>
    val params = request.queryString.map { case (k, v) => k -> v.mkString }

    if (!params.contains("id")) {
      Future(Ok("No id parameter in query"))
    } else {

      try {
        val id = params("id").toLong
        val categories = categoryRepository.getByIdOption(id)

        categories.map(category => category match {
          case Some(c) => {
            val name = if (params.contains("name")) params("name") else c.name

            val newCategory = Category(id, name)
            categoryRepository.update(id, newCategory)
            Ok("Category updated!")
          }
          case None => Ok("No object with such id")
        })
      } catch {
        case e: NumberFormatException => Future(Ok("Id has to be integer"))
      }

    }
  }

  def delete = SecuredAction { implicit request =>
    val params = request.queryString.map { case (k, v) => k -> v.mkString }

    if (!params.contains("id")) {
      Ok("No id parameter in query")
    } else {
      try {
        categoryRepository.delete(params("id").toLong)
        productCategoryRepository.deleteCategory(params("id").toLong)
        Ok("Category deleted!")
      } catch {
        case e: NumberFormatException => Ok("Id has to be integer")
      }
    }
  }

}