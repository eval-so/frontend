package controllers

import models._
import play.api._
import play.api.mvc._
import play.api.data._
import play.api.Play.current
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import com.typesafe.plugin._

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
      "email"    -> email,
      "password" -> tuple(
        "main"    -> text(minLength = 8),
        "confirm" -> text
      ).verifying(
        "Passwords must match", password => password._1 == password._2
      ),
      "accept"   -> checked("You must accept the terms and conditions.")
    )
    {
      val salt = java.util.UUID.randomUUID().toString
      (name, username, email, password, _) => User(
        None,
        username,
        Application.sha256(salt + password._1),
        salt,
        name,
        email,
        None,
        None,
        java.util.UUID.randomUUID().toString
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

  /** Handle registration of new users.
    *
    * @todo fix `success`'s line length.
    */
  def register = Action { implicit request =>
    registerForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.index(formWithErrors))
      },
      valid => {
        val mail = use[MailerPlugin].email
        mail.setSubject("Welcome to Breakpoint!")
        mail.addRecipient("%s <%s>".format(valid.name, valid.email))
        mail.addFrom("Breakpoint Eval <noreply@breakpoint-eval.org>")
        mail.send("""Welcome to Breakpoint!
                  |
                  |Before you can begin using your account, we need to confirm
                  |that this is a valid email addresss, and that you meant to
                  |sign up.
                  |
                  |If you didn't intend to sign up for Breakpoint, please
                  |disregard this email.
                  |
                  |However, if you intended to sign up, you must activate your
                  |account, by clicking visiting this link:
                  |http://%s/confirm/%s
                  |
                  |We hope you enjoy using Breakpoint.""".stripMargin.format(
                    request.host,
                    valid.confirmationToken))
        User.add(valid)
        Redirect(routes.Application.index).flashing(
          "success" -> "Welcome aboard. Please check your email for details on where to go from here."
        )
      }
    )
  }

  /** Confirm a user's account based on their UUID token. */
  def confirmRegistration(userID: Long, confirmationToken: String) = Action { implicit redirect =>
    val user = User.getByID(userID)
    user match {
      case Some(user) => {
        if (user.confirmationToken == confirmationToken) {
          user.confirm()
          Ok(views.html.user.confirm())
        } else {
          BadRequest
        }
      }
      case None => BadRequest
    }
  }
}
