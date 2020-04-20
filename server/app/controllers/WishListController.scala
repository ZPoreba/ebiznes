package controllers

import javax.inject._
import models.{ProductRepository, UserRepository, WishListProductRepository}
import play.api.mvc._
import play.filters.csrf.CSRF

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class WishListController @Inject()(wishListProductRepository: WishListProductRepository,
                                   userRepository: UserRepository,
                                   productRepository: ProductRepository,
                                   cc: MessagesControllerComponents) (implicit ec: ExecutionContext) extends MessagesAbstractController(cc){

  def getToken = Action { implicit request =>
    val token = CSRF.getToken.get.value
    Ok(token)
  }

  def create = Action.async { implicit request =>
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
              wishListProductRepository.create(userId, prodId)
            }
          })
        })
      }
      Future(Ok("WishList created!"))
    }
  }

  def read: Action[AnyContent] = Action.async { implicit request =>
    val wishLists = wishListProductRepository.list()
    wishLists.map( wishList => Ok(wishList.toString()) )
  }

  def readById: Action[AnyContent] = Action.async { implicit request =>

    val params = request.queryString.map { case (k,v) => k -> v.mkString }
    if (!params.contains("id")) {
      Future(Ok("No id parameter in query"))
    }
    else {
      val wishLists = wishListProductRepository.getByUserId(params("id").toLong)
      wishLists.map(wishList => Ok(wishList.toString()))
    }

  }

  def update = Action { implicit request =>
    Ok("WishList update not needed if we assume existence of wishlist-product table. " +
      "Than update means deleting old relation and create new one.")
  }

  // By user id
  def delete = Action { implicit request =>
    val params = request.queryString.map { case (k,v) => k -> v.mkString }

    if (!params.contains("id")) {
      Ok("No id parameter in query")
    }
    else {
      try {
        wishListProductRepository.deleteUser(params("id").toLong)
        Ok("WishList for user with id " + params("id") + " deleted!")
      }
      catch {
        case e: NumberFormatException => Ok("Id has to be integer")
      }
    }
  }

}
