package models

import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current
import java.util.Date
import org.joda.time.{DateTime, Period}
import org.joda.time.format.{DateTimeFormat, PeriodFormat}

/** A Breakpoint user.
  *
  * @constructor interact with a breakpoint user
  * @param id the database-given ID of the user
  * @param username the user's username
  * @param password a sha256 of the user's password
  * @param salt a salt prepended to the password before it's hashed
  * @param name the user's real name
  * @param email the user's email address
  */
case class User(id: Long,
                username: String,
                password: String,
                salt: String,
                name: String,
                email: String) {

  /** The Breakpoint Applications that the User has access to.
    *
    * This includes both applications that the user owns and applications
    * that they have authorized. These can be filtered like so:
    *
    * {{{
    * val user = User.getByID(1)
    * val applications = user.applications
    * val ownedApplications = applications.filter { _.owner }
    * }}}
    *
    * @return a sequence containing BreakpointApplication instances
    */
//  lazy val applications: Seq[BreakpointApplication] =
//    BreakpointApplicationUser.getByUserID(id).applications
}

object User {
  val simple = {
    get[Long]("id") ~
    get[String]("username") ~
    get[String]("password") ~
    get[String]("hash") ~
    get[String]("name") ~
    get[String]("email") map {
      case id~username~password~hash~name~email =>
        User(id, username, password, hash, name, email)
     }
  }

  /** Fetch a Breakpoint user by their ID.
    *
    * @param id the database-given ID for the user
    * @return an Option[User] depending on whether or not a valid user was
    *         found
    */
  def getByID(id: Long): Option[User] = DB.withConnection { implicit c =>
    SQL("SELECT * FROM users WHERE id={id}").on(
      'id -> id
    ).as(User.simple.singleOpt)
  }
}
