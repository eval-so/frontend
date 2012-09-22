package models

import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play.current
import java.util.Date
import org.joda.time.{DateTime, Period}
import org.joda.time.format.{DateTimeFormat, PeriodFormat}

/** A Breakpoint application
  *
  * @constructor interact with a breakpoint application
  * @param id the database-given ID of the application
  * @param name the name of the application
  * @param apiID the public UUID identifier for the application
  * @param createdAt a [[joda.time.DateTime]] of when the app was created
  */
case class BreakpointApplication(
  id: Option[Long],
  name: String,
  apiID: String,
  createdAt: Option[DateTime],
  allowAnonymousEvals: Boolean) {

  /** The Breakpoint Users who have authorized (or own) the application.
    *
    * @return a sequence containing [[models.User]] instances
    */
//  lazy val applications: Seq[BreakpointApplication] =
//    BreakpointApplicationUser.getByUserID(id).applications
}

object BreakpointApplication {
  val simple = {
    get[Option[Long]]("id") ~
    get[String]("name") ~
    get[String]("api_id") ~
    get[Option[Date]]("created_at") ~
    get[Boolean]("allow_anonymous_evals") map {
      case id~name~apiID~createdAt~allowAnonymousEvals =>
        BreakpointApplication(
          id,
          name,
          apiID,
          createdAt match {
            case Some(date) => Some(new DateTime(date))
            case None => None
          },
          allowAnonymousEvals)
     }
  }

  /** Fetch a Breakpoint application by its ID.
    *
    * @param id the database-given ID for the application
    * @return an Option[BreakpointApplication] depending on whether or not a
    *         valid application was found for the given id.
    */
  def getByID(id: Long): Option[BreakpointApplication] = DB.withConnection {
    implicit c =>
      SQL("SELECT * FROM applications WHERE id={id}").on(
        'id -> id
      ).as(BreakpointApplication.simple.singleOpt)
  }

  /** Fetch all Breakpoint applications for a given user ID.
    *
    * @param userID the ID of the user
    * @return a Seq[BreakpointApplication] of all the user's applications.
    */
  def getAllByUserID(userID: Long): Seq[BreakpointApplication] =
    DB.withConnection { implicit c =>
      SQL("SELECT * FROM applications WHERE id={id}").on(
        'id -> userID
      ).as(BreakpointApplication.simple *)
  }
}
