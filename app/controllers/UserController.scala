package controllers

import models._
import play.api._
import play.api.mvc._
import play.api.data._
import play.api.Play.current
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import com.typesafe.plugin._
import jp.t2v.lab.play20.auth._


/** Controllers which deal with authentication. */
object UserController extends Controller with Auth with LoginLogout with AuthConfigImpl {

  /** A form which allows users to log in. */
  val loginForm = Form(
    mapping(
      "username" -> nonEmptyText,
      "password" -> nonEmptyText
    )(User.authenticate)(_.map(u => (u.email, ""))).verifying(
      "Invalid username/password combination",
      result => result.isDefined
    )
  )

  /** A form to handle registration and profile changing.
    *
    * We handle validations, and password salt generation here as well.
    */
  val registerForm: Form[User] = Form(
    mapping(
      "name"    -> nonEmptyText,
      "username" -> nonEmptyText.verifying(
        "Sorry, that username is already registered to another account",
        username => !User.usernameIsTaken(username)),
      "email"    -> email.verifying(
        "Sorry, that email is already registered to another account",
        email => !User.emailIsTaken(email)),
      "password" -> text(minLength = 8)
    )
    {
      val salt = java.util.UUID.randomUUID().toString
      (name, username, email, password) => User(
        None,
        username,
        Application.sha256(salt + password),
        salt,
        name,
        email,
        None,
        None,
        java.util.UUID.randomUUID().toString,
        java.util.UUID.randomUUID().toString
      )
    }
    {
      user => Some(
        user.name,
        user.username,
        user.email,
        user.password
      )
    }
  )

  /** Handle registration of new users. */
  def register = optionalUserAction { user => implicit request =>
    user match {
      case Some(user) => Redirect(routes.Application.index).flashing(
        "error" -> "You're already signed in!")
      case None => {
        registerForm.bindFromRequest.fold(
          formWithErrors => {
            BadRequest(views.html.index(None, formWithErrors))
          },
          valid => {
            val Some(userID) = User.add(valid)
            val sendRegEmail = Play.application.configuration.getBoolean(
              "breakpoint.frontend.authentication.send_welcome_email").getOrElse(
              false)
            if (sendRegEmail) {
              val mail = use[MailerPlugin].email
              mail.setSubject("Welcome to Breakpoint!")
              mail.addRecipient("%s <%s>".format(valid.name, valid.email))
              mail.addFrom("Breakpoint Eval <noreply@breakpoint-eval.org>")
              mail.send(
                """Welcome to Breakpoint!
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
                |http://%s/confirm/%d/%s
                |
                |We hope you enjoy using Breakpoint.""".stripMargin.format(
                  request.host,
                  userID,
                  valid.confirmationToken))
            }
            Redirect(routes.Application.index).flashing(
              "signup.success" -> "Welcome aboard. Please check your email for details on where to go from here."
            )
          }
        )
      }
    }
  }

  /** Confirm a user's account based on their UUID token. */
  def confirmRegistration(userID: Long, confirmationToken: String) = Action { implicit request =>
    val user = User.getByID(userID)
    user match {
      case Some(user) => {
        if (user.confirmationToken == confirmationToken) {
          user.confirm()
          gotoLoginSucceeded(user.id.get).flashing(
            "success" -> "You've confirmed your account. Thanks!")
        } else {
          BadRequest(
            views.html.error(
              "Incorrect validation token.",
              "That validation token was wrong, for the given user! :-("))
        }
      }
      case None => BadRequest(
        views.html.error(
          "Invalid user ID",
          "Yikes! That user ID wasn't found in our database."))
    }
  }

  /** Allow a user to attempt authentication. */
  def login = optionalUserAction { maybeUser => request =>
    Ok(views.html.user.login(loginForm))
  }

  /** Actually attempt authentication. */
  def authenticate = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.user.login(loginForm)),
      user => gotoLoginSucceeded(user.get.id.get)
    )
  }

  /** A quick test. */
  def isLoggedIn = optionalUserAction { maybeUser => request =>
    maybeUser match {
      case Some(user) => Ok("logged in.")
      case None => Ok("not logged in.")
    }
  }
}
