package controllers

import forms._

import scala.concurrent.Future
import com.mohiva.play.silhouette.api.LogoutEvent
import javax.inject.Inject
import play.api.mvc.Action

import scala.concurrent.ExecutionContext

class ApplicationController @Inject() (scc: SilhouetteControllerComponents)(implicit ex: ExecutionContext) extends SilhouetteController(scc) {

  def index = Action { implicit request =>
    Ok(views.html.index("Your new application is ready."))
//    Future.successful(Ok(views.html.home(request.identity)))
  }

  def signIn = silhouette.UserAwareAction.async { implicit request =>
    request.identity match {
      case Some(user) => Future.successful(Redirect(routes.ApplicationController.index()))
      case None => Future.successful(Ok(views.html.signIn(SignInForm.form, socialProviderRegistry)))
    }
  }

  def signUp = silhouette.UserAwareAction.async { implicit request =>
    request.identity match {
      case Some(user) => Future.successful(Redirect(routes.ApplicationController.index()))
      case None => Future.successful(Ok(views.html.signUp(SignUpForm.form)))
    }
  }

  def signOut = SecuredAction.async { implicit request =>
    val result = Ok("Signed out")
    eventBus.publish(LogoutEvent(request.identity, request))

    authenticatorService.discard(request.authenticator, result)
  }
}
