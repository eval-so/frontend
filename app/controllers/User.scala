package controllers

import models._
import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._

/** Controllers which deal with authentication. */
object UserController extends Controller {

  /** A form to handle registration and profile changing.
    *
    * We handle validations, and password salt generation here as well.
    *
    * @todo handle username validation (e.g. does it exist already?)
    */
  val registerForm: Form[User] = Form(
    mapping(
      "name"    -> nonEmptyText,
      "username" -> nonEmptyText,
      "email"    -> nonEmptyText,
      "password" -> tuple(
        "main"    -> text(minLength = 8),
        "confirm" -> text
      ).verifying(
        "Passwords must match", password => password._1 == password._2
      ),
      "accept"   -> checked("You must accept the terms and conditions.")
    )
    {
      (name, username, email, password, _) => User(
        None,
        username,
        password._1,
        java.util.UUID.randomUUID().toString,
        name,
        email
      )
    }
    {
      user => Some(
        user.name,
        user.username,
        user.email,
        (user.password, ""),
        false
      )
    }
  )

  /** Handle registration of new users. */
  def register = Action {
    Ok(views.html.user.register(registerForm))
  }
}