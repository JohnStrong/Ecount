package controllers

import play.api.mvc._
import play.filters.csrf._

import helpers.form.FormHelper

object ViewController extends Controller {

  private val SESSION_ID_KEY = "user.id"

  private def getFormDependencies = {
    (FormHelper.loginForm, FormHelper.registerForm)
  }

  def index = CSRFAddToken {
    Action { implicit request =>
      if(!session.get(SESSION_ID_KEY).isDefined) {
        val (loginForm, registerForm) = getFormDependencies
        Ok(views.html.main(loginForm, registerForm))
      } else {
        Ok(views.html.portal())
      }
    }
  }

  def auth() = CSRFAddToken {
    Action { implicit request =>
      val (loginForm, registerForm) = getFormDependencies

      Ok(views.html.auth(loginForm, registerForm,
          flash.get("auth message")
        )
      )
    }
  }

  def requestPasswordReset  = CSRFAddToken {
    Action { implicit request => {
      val passwordResetForm = FormHelper.registerForm
      Ok(views.html.resetPassword(passwordResetForm))
    }}
  }

  // load confirmation view with new session (fixes csrf failed error)
  def confirmation(email: String) = Action {
    Ok(views.html.confirmation(email)).withNewSession
  }
}