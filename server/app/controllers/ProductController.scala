package controllers

import javax.inject._
import models.{Product, ProductCategoryRepository}
import play.api.mvc._
import play.filters.csrf.CSRF
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ProductController @Inject()(productCategoryRepository: ProductCategoryRepository, cc: MessagesControllerComponents)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  def getToken = Action { implicit request =>
    val token = CSRF.getToken.get.value
    Ok(token)
  }

  def create:Action[AnyContent] = Action.async { implicit request =>
    val params = request.queryString.map { case (k,v) => k -> v.mkString }
    if (!params.contains("name")) {
      Future(Ok("No name parameter in query"))
    }
    else if (!params.contains("description")) {
      Future(Ok("No description parameter in query"))
    }
    else if (!params.contains("price")) {
      Future(Ok("No price parameter in query"))
    }
    else if (!params.contains("categories")) {
      Future(Ok("No categories parameter in query"))
    }
    else {
      val catArray = params("categories").replaceAll(" ", "").split( ',' )
      val longCatArray = catArray.map(_.toLong)
      productCategoryRepository.create(params("name"), params("description"), params("price").toInt, longCatArray).map(id => Ok(id.toString))
    }
  }

  def read: Action[AnyContent] = Action.async { implicit request =>
    val products = productCategoryRepository.list()
    products.map( product => Ok(product.toString()) )
  }

  def readById: Action[AnyContent] = Action.async { implicit request =>

    val params = request.queryString.map { case (k,v) => k -> v.mkString }
    if (!params.contains("id")) {
      Future(Ok("No id parameter in query"))
    }
    else {
      val products = productCategoryRepository.getById(params("id").toLong)
      products.map(product => Ok(product.toString()))
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
        val products = productCategoryRepository.getById(id)

        products.map(product => product match {
          case Some(p) => {
            val name = if (params.contains("name")) params("name") else p.name
            val description = if (params.contains("description")) params("description") else p.description
            val price = if (params.contains("price")) params("price").toInt else p.price
            val categories = if (params.contains("categories")) params("categories").replaceAll(" ", "")
              .split(',')
              .map(_.toLong) else Array[Long]()

            if (categories.size != 0) productCategoryRepository.deleteProductCategories(id) // if categories are not empty, delete old one

            val newProduct = Product(id, name, description, price)
            productCategoryRepository.update(id, newProduct, categories)
            Ok("Product updated!")
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
        productCategoryRepository.deleteProduct(params("id").toLong)
        Ok("Product deleted!")
      }
      catch {
        case e: NumberFormatException => Ok("Id has to be integer")
      }
    }
  }

}
