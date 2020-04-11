package controllers

import javax.inject._
import play.api.mvc._
import play.filters.csrf.CSRF


@Singleton
class WishListController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  def getToken = Action { implicit request =>
    val token = CSRF.getToken.get.value
    Ok(token)
  }

  def create = Action {
    Ok("WishList created!")
  }

  def read = Action {
    Ok("WishList read!")
  }

  def update = Action { implicit request =>
    Ok("WishList updated!")
  }

  def delete = Action { implicit request =>
    Ok("WishList deleted!")
  }

}
