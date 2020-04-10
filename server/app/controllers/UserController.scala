package controllers

import javax.inject._
import play.api.mvc._
import play.filters.csrf.CSRF


@Singleton
class UserController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  def getToken = Action { implicit request =>
    val token = CSRF.getToken.get.value
    Ok(token)
  }

  def create = Action {
    Ok("User created!")
  }

  def read = Action {
    Ok("User read!")
  }

  def update = Action { implicit request =>
    Ok("User updated!")
  }

  def delete = Action { implicit request =>
    Ok("User deleted!")
  }

}
