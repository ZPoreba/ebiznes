package controllers

import javax.inject._
import play.api.mvc._
import play.filters.csrf.CSRF


@Singleton
class DiscountCodeController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  def getToken = Action { implicit request =>
    val token = CSRF.getToken.get.value
    Ok(token)
  }

  def create = Action {
    Ok("DiscountCode created!")
  }

  def read = Action {
    Ok("DiscountCode read!")
  }

  def update = Action { implicit request =>
    Ok("DiscountCode updated!")
  }

  def delete = Action { implicit request =>
    Ok("DiscountCode deleted!")
  }

}
