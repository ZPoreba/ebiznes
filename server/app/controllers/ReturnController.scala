package controllers

import javax.inject._
import models.{ApiUser, Product, ProductRepository, Return, ReturnRepository, User, UserRepository}
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.data.Forms._
import play.api.mvc._
import play.filters.csrf.CSRF

import scala.collection.mutable.ListBuffer
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}


@Singleton
class ReturnController @Inject()(returnRepository: ReturnRepository,
                                 productRepository: ProductRepository,
                                 userRepository: UserRepository,
                                 cc: MessagesControllerComponents)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  val createForm: Form[CreateReturnForm] = Form {
    mapping(
      "userId" -> nonEmptyText,
      "productId" -> longNumber,
      "status" -> nonEmptyText,
    )(CreateReturnForm.apply)(CreateReturnForm.unapply)
  }

  val updateForm: Form[UpdateReturnForm] = Form {
    mapping(
      "id" -> longNumber,
      "userId" -> nonEmptyText,
      "productId" -> longNumber,
      "status" -> nonEmptyText,
    )(UpdateReturnForm.apply)(UpdateReturnForm.unapply)
  }

  def create:Action[AnyContent] = Action { implicit request =>
    val users = Await.result(userRepository.list(), Duration.Inf)
    val products = Await.result(productRepository.list(), Duration.Inf)
    Ok(views.html.returnadd(createForm, users, products))
  }

  def createHandle = Action.async { implicit request =>
    var usr = Await.result(userRepository.list(), Duration.Inf)
    var prod = Await.result(productRepository.list(), Duration.Inf)

    createForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.returnadd(errorForm, usr, prod))
        )
      },
      return_t => {
        returnRepository.create(return_t.userId, return_t.productId, return_t.status).map { id =>
          Redirect(routes.ReturnController.create()).flashing("success" -> "return.created")
        }
      }
    )

  }

  def read: Action[AnyContent] = Action { implicit request =>
    val returns = Await.result(returnRepository.list(), Duration.Inf)
    Ok(views.html.returnsread(returns))
  }

  def readById(id: Long): Action[AnyContent] = Action.async { implicit request =>
    val return_t = returnRepository.getByIdOption(id)

    return_t.map(ret => ret match {
      case Some(r) => Ok(views.html.returnread(r))
      case None => Redirect(routes.ReturnController.read())
    })
  }

  def update(id: Long):Action[AnyContent] = Action { implicit request =>

    val return_t_result = Await.result(returnRepository.getById(id), Duration.Inf)
    val users = Await.result(userRepository.list(), Duration.Inf)
    val products = Await.result(productRepository.list(), Duration.Inf)

    val returnForm = updateForm.fill( UpdateReturnForm(return_t_result.id, return_t_result.userId, return_t_result.productId, return_t_result.status))
    Ok(views.html.returnupdate(returnForm, users, products))

  }

  def updateHandle = Action.async { implicit request =>

    var usr:Seq[ApiUser] = Seq[ApiUser]()
    val users = userRepository.list().onComplete{
      case Success(u) => usr = u
      case Failure(_) => print("fail")
    }

    var prod:Seq[Product] = Seq[Product]()
    val products = productRepository.list().onComplete{
      case Success(p) => prod = p
      case Failure(_) => print("fail")
    }

    val users_result = Await.result(userRepository.list(), Duration.Inf)
    val products_result = Await.result(productRepository.list(), Duration.Inf)

    updateForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.returnupdate(errorForm, users_result, products_result))
        )
      },
      return_t => {
        returnRepository.update(return_t.id, Return(return_t.id, return_t.userId, return_t.productId, return_t.status)).map { _ =>
          Redirect(routes.ReturnController.update(return_t.id)).flashing("success" -> "return updated")
        }
      }
    )

  }

  def delete(id: Long): Action[AnyContent] = Action {
    returnRepository.delete(id)
    Redirect("/readreturns")
  }

}

case class CreateReturnForm(userId: String, productId: Long, status: String)
case class UpdateReturnForm(id: Long, userId: String, productId: Long, status: String)