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
case class BreakpointApplication(id: Long,
                                 name: String,
                                 apiID: String,
                                 createdAt: DateTime) {

  /** The Breakpoint Users who have authorized (or own) the application.
    *
    * @return a sequence containing [[models.User]] instances
    */
//  lazy val applications: Seq[BreakpointApplication] =
//    BreakpointApplicationUser.getByUserID(id).applications
}

object BreakpointApplication {
  val simple = {
    get[Long]("id") ~
    get[String]("name") ~
    get[String]("api_id") ~
    get[Date]("created_at") map {
      case id~name~apiID~createdAt =>
        BreakpointApplication(id, name, apiID, new DateTime(createdAt))
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
}
