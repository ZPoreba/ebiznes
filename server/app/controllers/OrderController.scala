package controllers

import javax.inject._
import play.api.mvc._
import play.filters.csrf.CSRF


@Singleton
class OrderController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  def getToken = Action { implicit request =>
    val token = CSRF.getToken.get.value
    Ok(token)
  }

  def create = Action {
    Ok("Order created!")
  }

  def read = Action {
    Ok("Order read!")
  }

  def update = Action { implicit request =>
    Ok("Order updated!")
  }

  def delete = Action { implicit request =>
    Ok("Order deleted!")
  }

}
