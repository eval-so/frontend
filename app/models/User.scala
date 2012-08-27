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
  * @param registeredAt the time that the user initially registered
  * @param confirmedAt the time that the user confirmed their account
  * @param confirmationToken a unique ID with which the user validates their
  *                          intent to register
  */
case class User(
  id: Option[Long],
  username: String,
  password: String,
  salt: String,
  name: String,
  email: String,
  registeredAt: Option[DateTime],
  confirmedAt: Option[DateTime],
  confirmationToken: String) {

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

  /** Confirm a user's registration, after they've gone to a URL containing
    * their super sekrit UUID token.
    */
  def confirm() = DB.withConnection { implicit c =>
    SQL("""
        UPDATE users SET confirmed_at=CURRENT_TIMESTAMP
        WHERE id={id} AND confirmed_at IS NULL
        """).on(
      'id -> id.get
    ).execute()
  }
}

object User {
  /** Represent a single user.
    *
    * @todo fix line length of `case` below.
    */
  val simple = {
    get[Option[Long]]("id") ~
    get[String]("username") ~
    get[String]("password") ~
    get[String]("salt") ~
    get[String]("name") ~
    get[String]("email") ~
    get[Option[Date]]("registered_at") ~
    get[Option[Date]]("confirmed_at") ~
    get[String]("confirmation_token") map {
      case id~username~password~salt~name~email~registeredAt~confirmedAt~confirmationToken =>
        User(
          id,
          username,
          password,
          salt,
          name,
          email,
          registeredAt match {
            case Some(date) => Some(new DateTime(date))
            case None => None
          },
          confirmedAt match {
            case Some(date) => Some(new DateTime(date))
            case None => None
          },
          confirmationToken)
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

  /** Add a user to the database.
    *
    * @param username the user's username
    * @param email the user's email address
    * @param password the user's password
    * @param name the user's real name
    * @return a Long which is the user's ID in the database.
    */
  def add(user: User): Option[Long] =
    DB.withConnection { implicit c =>
      val salt = java.util.UUID.randomUUID()
      SQL(
        """
        INSERT INTO users(
          username,
          password,
          salt,
          name,
          email,
          confirmation_token)
        VALUES(
          {username},
          {password},
          {salt},
          {name},
          {email},
          {confirmation_token})
        """).on(
          'username -> user.username,
          'password -> user.password,
          'salt -> user.salt,
          'name -> user.name,
          'email -> user.email,
          'confirmation_token -> user.confirmationToken).executeInsert()
    }

  /** Check to see if a user exists, given an email address.
    *
    * @param email the email address to look up
    * @return a Boolean, true if the email address is already used, false if not
    */
  def emailIsTaken(email: String) = DB.withConnection { implicit c =>
    val count = SQL("SELECT COUNT(*) FROM users WHERE email={email}").on(
      'email -> email
    ).as(scalar[Long].single)
    if (count == 0) false else true
  }

  /** Check to see if a user exists, given a username.
    *
    * @param username the username to look up
    * @return a Boolean, true if the username is already used, false if not
    */
  def usernameIsTaken(username: String) = DB.withConnection { implicit c =>
    val count = SQL("SELECT COUNT(*) FROM users WHERE username={username}").on(
      'username -> username
    ).as(scalar[Long].single)
    if (count == 0) false else true
  }
}
