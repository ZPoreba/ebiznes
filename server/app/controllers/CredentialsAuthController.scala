package controllers

import com.mohiva.play.silhouette.api.Authenticator.Implicits._
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.util.Credentials
import com.mohiva.play.silhouette.impl.providers._
import forms.SignInForm
import net.ceedubs.ficus.Ficus._
import play.api.Configuration
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps
import javax.inject.Inject
import play.api.libs.json.Json
import scala.concurrent.ExecutionContext

class CredentialsAuthController @Inject() (
  scc: SilhouetteControllerComponents,
  credentialsProvider: CredentialsProvider,
  configuration: Configuration)(implicit ex: ExecutionContext) extends SilhouetteController(scc) {

  def authenticate = Action.async { implicit request =>
    SignInForm.form.bindFromRequest.fold(
      _ => Future.successful(BadRequest),
      data => {
        val credentials = Credentials(data.email, data.password)
        credentialsProvider.authenticate(credentials).flatMap { loginInfo =>
          userService.retrieve(loginInfo).flatMap {
            case Some(user) =>
              val c = configuration.underlying
              authenticatorService.create(loginInfo).map {
                case authenticator if data.rememberMe =>
                  authenticator.copy(
                    expirationDateTime = clock.now + c.as[FiniteDuration]("silhouette.authenticator.rememberMe.authenticatorExpiry"),
                    idleTimeout = c.getAs[FiniteDuration]("silhouette.authenticator.rememberMe.authenticatorIdleTimeout"),
                    cookieMaxAge = c.getAs[FiniteDuration]("silhouette.authenticator.rememberMe.cookieMaxAge"))
                case authenticator => authenticator
              }.flatMap { authenticator =>
                eventBus.publish(LoginEvent(user, request))
                authenticatorService.init(authenticator).flatMap { v =>
                  authenticatorService.embed(v,
                    Ok(Json.toJson(user))
                  )
                }
              }
            case None => Future.successful(Forbidden(Json.obj("errorCode" -> "User Not Found")))
          }
        }.recover {
          case e: ProviderException =>
            InternalServerError(Json.obj("errorCode" -> "Wrong Credentials"))
        }
      })
  }
}
