/** (c) 2012 Ricky Elrod <ricky@elrod.me>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */

package controllers

import models._
import play.api._
import play.api.mvc._
import play.api.data._
import play.api.Play.current
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import jp.t2v.lab.play20.auth._
import breakpoint.util.{BreakpointEmail}

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
      (username, email, password) => User(
        None,
        username,
        Application.sha256(salt + password),
        salt,
        None,
        email,
        None,
        None,
        java.util.UUID.randomUUID().toString,
        java.util.UUID.randomUUID().toString
      )
    }
    {
      user => Some(
        user.username,
        user.email,
        user.password
      )
    }
  )

  /** A form to handle profile updating. */
  def profileForm(implicit rUser: models.User) = Form(
    mapping(
      "name" -> text,
      "email" -> email,
      "new_password" -> text,
      "old_password" -> nonEmptyText.verifying(
        "Must match current account password",
        oldPassword => {
          Application.sha256(rUser.salt + oldPassword) == rUser.password
        }
      )
    )
    {
      (name, email, newPassword, oldPassword) => {
        val salt = if (!newPassword.isEmpty) {
          java.util.UUID.randomUUID().toString
        } else {
          ""
        }

        val password = if (!newPassword.isEmpty) {
          Application.sha256(salt + newPassword)
        } else {
          ""
        }

        Map(
          // DB column -> value
          "name" -> name,
          "email" -> email,
          "new_salt" -> salt,
          "new_password" -> password,
          "old_password" -> oldPassword
        )
      }
    }
    {
      data => Some(
        data("name"),
        data("email"),
        data("new_password"),
        data("old_password"))
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
              val registrationEmail = BreakpointEmail.send(
                "%s <%s>".format(valid.name, valid.email),
                "Welcome to Breakpoint!",
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
              registrationEmail.fold(
                left => {
                  // TODO: send site admins an email.
                  // TODO: inform Play who site admins are.
                  // TODO: inform site admins what Play is.
                  // TODO: inform site playmins what ads are. Or something.
                  Redirect(routes.Application.index).flashing(
                    "signup.failure" -> "Oops, an error has occurred. We got your registration information, but couldn't send you the validation email for some reason. We'll manually validate your account, and email you when you can begin using Breakpoint. Sorry for the delay!"
                  )
                },
                right => Redirect(routes.Application.index).flashing(
                  "signup.success" -> "Welcome aboard. Please check your email for details on where to go from here."
                )
              )
            } else {
              Redirect(routes.Application.index).flashing(
                "signup.success" -> "Welcome. You account will be looked over/approved, and we'll email you when that happens."
              )
            }
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
          user.confirmedAt match {
            case None => {
              user.confirm()
              gotoLoginSucceeded(user.id.get).flashing(
                "success" -> "You've confirmed your account. Thanks!")
            }
            case _ => BadRequest(
              views.html.error(
                "Account is already confirmed.",
                "This account has already been confirmed."))
          }
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
    maybeUser match {
      case None => Ok(views.html.user.login(loginForm))
      case _ => Redirect(routes.Application.index).flashing(
        "error" -> "Oops, you're already logged in!"
      )
    }
  }

  /** Allow a user to log out. */
  def logout = optionalUserAction { maybeUser => implicit request =>
    gotoLogoutSucceeded
  }


  /** Actually attempt authentication. */
  def authenticate = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.user.login(loginForm)),
      user => gotoLoginSucceeded(user.get.id.get)
    )
  }

  /** Allow a user to change their secret key. */
  def changeSecretKey = authorizedAction("user") { user => implicit request =>
    user.resetSecretKey()
    Redirect(routes.UserController.editProfile).flashing(
      "success" -> "Your secret key has been changed."
    )
  }

  /** A quick test. */
  def isLoggedIn = optionalUserAction { maybeUser => request =>
    maybeUser match {
      case Some(user) => Ok("logged in.")
      case None => Ok("not logged in.")
    }
  }

  /** Show profile updating form. */
  def editProfile = authorizedAction("user") { user => implicit request =>
    val existingData = Map(
      "name" -> user.name.getOrElse(""),
      "email" -> user.email,
      "new_password" -> "",
      "old_password" -> "")
    Ok(views.html.user.profile(user, profileForm(user).fill(existingData)))
  }

  /** Handle updating of profiles. */
  def updateProfile = authorizedAction("user") { user => implicit request =>
    profileForm(user).bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.user.profile(user, formWithErrors)),
      profile => {
        user.update(
          profile("new_password"),
          profile("new_salt"),
          if (!profile("name").isEmpty) Some(profile("name")) else None,
          profile("email"))
        Redirect(routes.UserController.editProfile).flashing(
          "success" -> "Profile edited successfully."
        )
      }
    )
  }
}
