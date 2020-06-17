package controllers

import javax.inject._
import models.{CartProduct, CartProductRepository, Product, ProductRepository, UserRepository}
import play.api.data.Form
import play.api.data.Forms.{mapping, number, seq}
import play.api.mvc._
import play.filters.csrf.CSRF
import play.api.data.Forms._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.collection.mutable.ListBuffer
import scala.util.{Failure, Success}

@Singleton
class CartController @Inject()(cartProductRepository: CartProductRepository,
                               userRepository: UserRepository,
                               productRepository: ProductRepository,
                               cc: MessagesControllerComponents) (implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {


  val createForm: Form[CreateCartForm] = Form {
    mapping(
      "userId" -> nonEmptyText,
      "product" -> seq(number),
    )(CreateCartForm.apply)(CreateCartForm.unapply)
  }

  val updateForm: Form[UpdateCartForm] = Form {
    mapping(
      "userId" -> nonEmptyText,
      "product" -> seq(number),
    )(UpdateCartForm.apply)(UpdateCartForm.unapply)
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

      Ok(views.html.cartadd(createForm, prod_list_seq, usr))
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
        BadRequest(views.html.cartadd(errorForm, prod_list_seq, usr))
      },
      product => {
        for (p <- product.product) {
          cartProductRepository.create(product.userId, p)
        }
        Redirect(routes.CartController.create()).flashing("success" -> "cart.created")
      }
    )
  }

  def read: Action[AnyContent] = Action { implicit request =>
    val users = Await.result(userRepository.list(), Duration.Inf)
    val cart_product = new ListBuffer[Seq[(String, Long)]]()

    for (u <- users) {
      val cart_product_result = Await.result(cartProductRepository.getByUserId(u.id), Duration.Inf)
      cart_product.+=(cart_product_result)
    }
    val prod_list_seq = cart_product.toList

    Ok(views.html.cartsread(users, prod_list_seq))
  }

  def readById(id: String): Action[AnyContent] = Action.async { implicit request =>
    val user = userRepository.getByIdOption(id)
    val cart_product = cartProductRepository.getByUserId(id)
    val cart_product_result = Await.result(cart_product, Duration.Inf)

    user.map(user => user match {
      case Some(u) => Ok(views.html.cartread(u, cart_product_result))
      case None => Redirect(routes.CartController.read())
    })
  }

  def update = Action { implicit request =>
    Ok("Basket update not needed if we assume existence of cart-product table. " +
      "Than update means deleting old relation and create new one.")
  }

  // By user id
  def delete(id: String): Action[AnyContent]  = Action {
    cartProductRepository.deleteUser(id)
    Redirect("/readcarts")
  }

}

case class CreateCartForm(userId: String, product: Seq[Int])
case class UpdateCartForm(userId: String, product: Seq[Int])