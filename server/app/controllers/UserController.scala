package controllers

import javax.inject._
import models.{ CartProductRepository, OpinionRepository, OrderRepository, UserRepository, WishListProductRepository }
import play.api.mvc._
import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, ExecutionContext }

@Singleton
class UserController @Inject() (
  userRepository: UserRepository,
  cartProductRepository: CartProductRepository,
  wishListProductRepository: WishListProductRepository,
  opinionRepository: OpinionRepository,
  orderRepository: OrderRepository,
  cc: MessagesControllerComponents)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  def readById(id: String): Action[AnyContent] = Action.async { implicit request =>
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

  def delete(id: String): Action[AnyContent] = Action {
    cartProductRepository.deleteUser(id)
    wishListProductRepository.deleteUser(id)
    userRepository.delete(id)
    opinionRepository.deleteUser(id)
    orderRepository.deleteUser(id)
    Redirect("/readusers")
  }

}

