package controllers

import javax.inject._
import models.{ProductRepository, UserRepository, WishListProductRepository}
import play.api.libs.json._
import play.api.mvc._
import play.filters.csrf.CSRF
import scala.collection.mutable.ListBuffer

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}


@Singleton
class ApiWishListController @Inject()(wishListProductRepository: WishListProductRepository,
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

  def read: Action[AnyContent] = Action { implicit request =>
    val wishLists = Await.result(wishListProductRepository.list(), Duration.Inf)
    val wishListMap = scala.collection.mutable.Map[Long, ListBuffer[Long]]()

    for (wishList <- wishLists) {

      if (wishListMap.contains(wishList._1)) {
        wishListMap(wishList._1)  +=  wishList._2
        wishListMap += (wishList._1 -> wishListMap(wishList._1))
      }
      else {
        val newValue = ListBuffer[Long]()
        newValue += wishList._2
        wishListMap += (wishList._1 -> newValue)
      }
    }
    
    Ok(Json.toJson(wishListMap))
  }

  def readById: Action[AnyContent] = Action { implicit request =>

    val params = request.queryString.map { case (k,v) => k -> v.mkString }
    if (!params.contains("id")) {
      Ok("No id parameter in query")
    }
    else {
      val id = params("id").toLong
      val wishList = Await.result(wishListProductRepository.getByUserId(id), Duration.Inf)
      val products = wishList.map( c => c._2 )

      val res = JsObject(Seq(
        ("id", Json.toJson(id)),
        ("products", Json.toJson(products))
      ))

      Ok(res)
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