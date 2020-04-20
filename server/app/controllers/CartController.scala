package controllers

import javax.inject._
import models.{CartProductRepository, ProductRepository, UserRepository}
import play.api.mvc._
import play.filters.csrf.CSRF

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class CartController @Inject()(cartProductRepository: CartProductRepository,
                               userRepository: UserRepository,
                               productRepository: ProductRepository,
                               cc: MessagesControllerComponents) (implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  def getToken = Action { implicit request =>
    val token = CSRF.getToken.get.value
    Ok(token)
  }

  def create:Action[AnyContent] = Action.async { implicit request =>
    val params = request.queryString.map { case (k,v) => k -> v.mkString }
    if (!params.contains("userId")) {
      Future(Ok("No userId parameter in query"))
    }
    else if (!params.contains("products")) {
      Future(Ok("No products parameter in query"))
    }
    else {
      val prodArray = params("products").replaceAll(" ", "").split( ',' )
      val longProdArray = prodArray.map(_.toLong)
      val userId = params("userId").toInt

      for (prodId <- longProdArray) {
        userRepository.exists(userId).map(userExists => {
          productRepository.exists(prodId).map(productExists => {
            if (userExists && productExists) {
              cartProductRepository.create(userId, prodId)
            }
          })
        })
      }
      Future(Ok("Cart created!"))
    }
  }

  def read: Action[AnyContent] = Action.async { implicit request =>
    val carts = cartProductRepository.list()
    carts.map( cart => Ok(cart.toString()) )
  }

  def readById: Action[AnyContent] = Action.async { implicit request =>

    val params = request.queryString.map { case (k,v) => k -> v.mkString }
    if (!params.contains("id")) {
      Future(Ok("No id parameter in query"))
    }
    else {
      val carts = cartProductRepository.getByUserId(params("id").toLong)
      carts.map(cart => Ok(cart.toString()))
    }

  }

  def update = Action { implicit request =>
    Ok("Basket update not needed if we assume existence of cart-product table. " +
      "Than update means deleting old relation and create new one.")
  }

  // By user id
  def delete = Action { implicit request =>
    val params = request.queryString.map { case (k,v) => k -> v.mkString }

    if (!params.contains("userId")) {
      Ok("No userId parameter in query")
    }
    else {
      try {
        cartProductRepository.deleteUser(params("userId").toLong)
        Ok("Bucket for user with id " + params("userId") + " deleted!")
      }
      catch {
        case e: NumberFormatException => Ok("Id has to be integer")
      }
    }
  }

}
