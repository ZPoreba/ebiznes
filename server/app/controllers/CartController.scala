package controllers

import javax.inject._
import play.api.mvc._
import play.filters.csrf.CSRF


@Singleton
class CartController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  def getToken = Action { implicit request =>
    val token = CSRF.getToken.get.value
    Ok(token)
  }

  def create = Action {
    Ok("Basket created!")
  }

  def read = Action {
    Ok("Basket read!")
  }

  def update = Action { implicit request =>
    Ok("Basket updated!")
  }

  def delete = Action { implicit request =>
    Ok("Basket deleted!")
  }

}
