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
  allowAnonymousEvals: Boolean,
  description: String) {

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
    get[Boolean]("allow_anonymous_evals") ~
    get[String]("description") map {
      case id~name~apiID~createdAt~allowAnonymousEvals~description =>
        BreakpointApplication(
          id,
          name,
          apiID,
          createdAt match {
            case Some(date) => Some(new DateTime(date))
            case None => None
          },
          allowAnonymousEvals,
          description)
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
  def getAllByUserID(userID: Long): List[BreakpointApplication] =
    DB.withConnection { implicit c =>
      SQL(
        """
        SELECT * FROM applications WHERE id IN (
          SELECT application_id FROM application_users WHERE user_id={user_id})
        """
      ).on(
        'user_id -> userID
      ).as(BreakpointApplication.simple *)
  }

  /** Add an application to the database.
    *
    * @param application A [[models.BreakpointApplication]] to add to the db.
    * @return a Long which is the application's ID in the database.
    */
  def add(application: BreakpointApplication): Option[Long] =
    DB.withConnection { implicit c =>
      SQL(
        """
        INSERT INTO applications(
          name,
          api_id,
          allow_anonymous_evals,
          description
        ) VALUES (
          {name},
          {api_id},
          {allow_anonymous_evals},
          {description}
        )
        """
      ).on(
        'name -> application.name,
        'api_id -> application.apiID,
        'allow_anonymous_evals -> application.allowAnonymousEvals,
        'description -> application.description).executeInsert()
    }
}
