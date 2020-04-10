package controllers

import javax.inject._
import play.api.mvc._
import play.filters.csrf.CSRF


@Singleton
class PaymentController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  def getToken = Action { implicit request =>
    val token = CSRF.getToken.get.value
    Ok(token)
  }

  def create = Action {
    Ok("Payment created!")
  }

  def read = Action {
    Ok("Payment read!")
  }

  def update = Action { implicit request =>
    Ok("Payment updated!")
  }

  def delete = Action { implicit request =>
    Ok("Payment deleted!")
  }

}
