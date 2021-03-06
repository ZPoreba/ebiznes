package controllers

import javax.inject._
import models.{ CartProductRepository, ProductRepository, UserRepository }
import play.api.libs.json._
import play.api.mvc._
import play.filters.csrf.CSRF
import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, ExecutionContext, Future }

@Singleton
class ApiCartController @Inject() (
  scc: SilhouetteControllerComponents,
  cartProductRepository: CartProductRepository,
  userRepository: UserRepository,
  productRepository: ProductRepository)(implicit ec: ExecutionContext) extends SilhouetteController(scc){

  def getToken = Action { implicit request =>
    val token = CSRF.getToken.get.value
    Ok(token)
  }

  def create: Action[AnyContent] = SecuredAction.async { implicit request =>
    val params = request.queryString.map { case (k, v) => k -> v.mkString }
    if (!params.contains("userId")) {
      Future(Ok("Error during adding product to bucket"))
    } else if (!params.contains("products")) {
      Future(Ok("Error during adding product to bucket"))
    } else {
      val prodArray = params("products").replaceAll(" ", "").split(',')
      val longProdArray = prodArray.map(_.toLong)
      val userId = params("userId")

      for (prodId <- longProdArray) {
        userRepository.exists(userId).map(userExists => {
          productRepository.exists(prodId).map(productExists => {
            if (userExists && productExists) {
              cartProductRepository.create(userId, prodId)
            }
          })
        })
      }
      Future(Ok("Product added successfully to bucket"))
    }
  }

  def read: Action[AnyContent] = SecuredAction.async { implicit request =>
    val carts = cartProductRepository.list()
    carts.map(cart => Ok(Json.toJson(cart)))
  }

  def readById: Action[AnyContent] = SecuredAction { implicit request =>

    val params = request.queryString.map { case (k, v) => k -> v.mkString }
    if (!params.contains("id")) {
      Ok("No id parameter in query")
    } else {
      val id = params("id")
      val cart = Await.result(cartProductRepository.getByUserId(id), Duration.Inf)
      val products = cart.map(c => c._2)

      val res = JsObject(Seq(
        ("id", Json.toJson(id)),
        ("products", Json.toJson(products))))

      Ok(res)
    }

  }

  def update = SecuredAction { implicit request =>
    Ok("Basket update not needed if we assume existence of cart-product table. " +
      "Than update means deleting old relation and create new one.")
  }

  // By user id
  def delete = SecuredAction { implicit request =>
    val params = request.queryString.map { case (k, v) => k -> v.mkString }

    if (!params.contains("userId")) {
      Ok("No userId parameter in query")
    } else {
      try {
        cartProductRepository.deleteUser(params("userId"))
        Ok("Bucket for user with id " + params("userId") + " deleted!")
      } catch {
        case e: NumberFormatException => Ok("Id has to be integer")
      }
    }
  }

  def deleteProductForUser = SecuredAction { implicit request =>
    val params = request.queryString.map { case (k, v) => k -> v.mkString }

    if (!params.contains("userId")) {
      Ok("Error during deleting product!")
    } else if (!params.contains("productId")) {
      Ok("Error during deleting product!")
    } else {
      try {
        cartProductRepository.deleteProductForUser(params("productId").toLong, params("userId"))
        Ok("Product with id " + params("productId") + " deleted!")
      } catch {
        case e: NumberFormatException => Ok("Error during deleting product!")
      }
    }
  }

}