package controllers

import javax.inject._
import models.{Product, ProductRepository, UserRepository, WishListProductRepository}
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._
import play.filters.csrf.CSRF

import scala.collection.mutable.ListBuffer
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}


@Singleton
class WishListController @Inject()(wishListProductRepository: WishListProductRepository,
                                   userRepository: UserRepository,
                                   productRepository: ProductRepository,
                                   cc: MessagesControllerComponents) (implicit ec: ExecutionContext) extends MessagesAbstractController(cc){

  val createForm: Form[CreateWishListForm] = Form {
    mapping(
      "userId" -> longNumber,
      "product" -> seq(number),
    )(CreateWishListForm.apply)(CreateWishListForm.unapply)
  }

  val updateForm: Form[UpdateWishListForm] = Form {
    mapping(
      "userId" -> longNumber,
      "product" -> seq(number),
    )(UpdateWishListForm.apply)(UpdateWishListForm.unapply)
  }

  def getToken = Action { implicit request =>
    val token = CSRF.getToken.get.value
    Ok(token)
  }

  def create:Action[AnyContent] = Action.async { implicit request =>
    var usr = Await.result(userRepository.list(), Duration.Inf)

    val products = productRepository.list()
    products.map (prod => {
      var prod_list = new ListBuffer[(String, String)]()
      for (p <- prod) {
        prod_list.+=((p.id.toString,p.name))
      }
      val prod_list_seq = prod_list.toList

      Ok(views.html.wishlistadd(createForm, prod_list_seq, usr))
    })
  }

  def createHandle = Action { implicit request =>
    var usr = Await.result(userRepository.list(), Duration.Inf)

    var produ:Seq[Product] = Seq[Product]()
    val products = productRepository.list().onComplete{
      case Success(prod) => produ = prod
      case Failure(_) => print("fail")
    }

    var prod_list = new ListBuffer[(String, String)]()
    for (p <- produ) {
      prod_list.+=((p.id.toString, p.name))
    }
    val prod_list_seq = prod_list.toList

    createForm.bindFromRequest.fold(
      errorForm => {
        BadRequest(views.html.wishlistadd(errorForm, prod_list_seq, usr))
      },
      product => {
        for (p <- product.product) {
          wishListProductRepository.create(product.userId, p)
        }
        Redirect(routes.WishListController.create()).flashing("success" -> "wishlist.created")
      }
    )
  }

  def read: Action[AnyContent] = Action { implicit request =>
    val users = Await.result(userRepository.list(), Duration.Inf)
    val wishlist_product = new ListBuffer[Seq[(Long, Long)]]()

    for (u <- users) {
      val wishlist_product_result = Await.result(wishListProductRepository.getByUserId(u.id), Duration.Inf)
      wishlist_product.+=(wishlist_product_result)
    }
    val prod_list_seq = wishlist_product.toList

    Ok(views.html.wishlistsread(users, prod_list_seq))
  }

  def readById(id: Long): Action[AnyContent] = Action.async { implicit request =>
    val user = userRepository.getByIdOption(id)
    val wishlist_product = wishListProductRepository.getByUserId(id)
    val wishlist_product_result = Await.result(wishlist_product, Duration.Inf)

    user.map(user => user match {
      case Some(u) => Ok(views.html.wishlistread(u, wishlist_product_result))
      case None => Redirect(routes.WishListController.read())
    })
  }

  def update = Action { implicit request =>
    Ok("WishList update not needed if we assume existence of wishlist-product table. " +
      "Than update means deleting old relation and create new one.")
  }

  // By user id
  def delete(id: Long): Action[AnyContent]  = Action {
    wishListProductRepository.deleteUser(id)
    Redirect("/readwishlists")
  }

}

case class CreateWishListForm(userId: Long, product: Seq[Int])
case class UpdateWishListForm(userId: Long, product: Seq[Int])