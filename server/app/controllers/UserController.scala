package controllers

import javax.inject._
import models.{CartProductRepository, OpinionRepository, OrderRepository, User, UserRepository, WishListProductRepository}
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.data.Forms._
import play.api.mvc._
import play.filters.csrf.CSRF

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}


@Singleton
class UserController @Inject()(userRepository: UserRepository,
                               cartProductRepository: CartProductRepository,
                               wishListProductRepository: WishListProductRepository,
                               opinionRepository: OpinionRepository,
                               orderRepository: OrderRepository,
                               cc: MessagesControllerComponents) (implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  val createForm: Form[CreateUserForm] = Form {
    mapping(
      "firstName" -> nonEmptyText,
      "secondName" -> nonEmptyText,
      "email" -> nonEmptyText,
      "password" -> nonEmptyText,
      "address" -> nonEmptyText,
    )(CreateUserForm.apply)(CreateUserForm.unapply)
  }

  val updateForm: Form[UpdateUserForm] = Form {
    mapping(
      "id" -> longNumber,
      "firstName" -> nonEmptyText,
      "secondName" -> nonEmptyText,
      "email" -> nonEmptyText,
      "password" -> nonEmptyText,
      "address" -> nonEmptyText,
    )(UpdateUserForm.apply)(UpdateUserForm.unapply)
  }


  def getToken = Action { implicit request =>
    val token = CSRF.getToken.get.value
    Ok(token)
  }

  def create: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.useradd(createForm))
  }

  def createHandle = Action.async { implicit request =>

    createForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.useradd(errorForm))
        )
      },
      user => {
        userRepository.create(user.firstName, user.secondName, user.email, user.password, user.address).map { _ =>
          Redirect(routes.UserController.create()).flashing("success" -> "category.created")
        }
      }
    )
  }

  def readById(id: Long): Action[AnyContent] = Action.async { implicit request =>
    val user = userRepository.getByIdOption(id)

    user.map(usr => usr match {
      case Some(u) => Ok(views.html.userread(u))
      case None => Redirect(routes.UserController.read())
    })
  }

  def read: Action[AnyContent] = Action { implicit request =>
    val users = Await.result(userRepository.list(), Duration.Inf)
    Ok(views.html.usersread(users))
  }

  def update(id: Long): Action[AnyContent] = Action { implicit request =>
    val user_result = Await.result(userRepository.getById(id), Duration.Inf)

    val usrForm = updateForm.fill(UpdateUserForm(user_result.id, user_result.firstName, user_result.secondName, user_result.email, user_result.password, user_result.address))
    Ok(views.html.userupdate(usrForm))
  }

  def updateHandle = Action.async { implicit request =>

    updateForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.userupdate(errorForm))
        )
      },
      user => {
        userRepository.update(user.id, User(user.id, user.firstName, user.secondName, user.email, user.password, user.address)).map { _ =>
          Redirect(routes.UserController.update(user.id)).flashing("success" -> "user updated")
        }
      }
    )

  }

  def delete(id: Long): Action[AnyContent] = Action {
    cartProductRepository.deleteUser(id)
    wishListProductRepository.deleteUser(id)
    userRepository.delete(id)
    opinionRepository.deleteUser(id)
    orderRepository.deleteUser(id)
    Redirect("/readusers")
  }

}

case class CreateUserForm(firstName: String, secondName: String, email: String, password: String, address: String)
case class UpdateUserForm(id: Long, firstName: String, secondName: String, email: String, password: String, address: String)
