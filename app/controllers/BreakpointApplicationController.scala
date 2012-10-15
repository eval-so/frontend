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

/** Controllers which deal with application management. */
object BreakpointApplicationController extends Controller with Auth with AuthConfigImpl {
  /** A form to handle creating new apps and editing existing ones. */
  val applicationForm: Form[BreakpointApplication] = Form(
    mapping(
      "name" -> nonEmptyText,
      "allow_anonymous_auth" -> checked("allow_anonymous_auth"),
      "description" -> nonEmptyText
    )
    {
      (name, allow_anonymous_auth, description) => BreakpointApplication(
        None,
        name,
        java.util.UUID.randomUUID().toString,
        None,
        allow_anonymous_auth,
        description
      )
    }
    {
      breakpointApplication => Some(
        breakpointApplication.name,
        breakpointApplication.allowAnonymousEvals,
        breakpointApplication.description
      )
    }
  )

  def newApplication = authorizedAction("user") { user => implicit request =>
    Ok(views.html.applications.newApplication(user, applicationForm))
  }

  def processNewApplication = authorizedAction("user") { user =>
    implicit request =>
      applicationForm.bindFromRequest.fold(
        formWithErrors => BadRequest(views.html.applications.newApplication(user, formWithErrors)),
        validApplication => {
          BreakpointApplication.add(validApplication) match {
            case Some(appID) => {
              user.addApplication(appID, true)
              Redirect(routes.BreakpointApplicationController.application(appID)).flashing(
                "success" -> "Your new application has been created."
              )
            }
            case _ => BadRequest(views.html.error(
              "Oops, the application couldn't be saved.",
              "An error occurred while trying to register the new application in our database. We've logged this error and will look into the issue. Please try again later. If you see this error again, please let us know, using the contact methods below."
            ))
          }
        }
      )
  }

  def myApplications = authorizedAction("user") { user => implicit request =>
    val applications = user.applications
    Ok(views.html.applications.myApplications(user, applications))
  }

  /** View a specific application's details. */
  def application(id: Long) = authorizedAction("user") { user => implicit request =>
    val application = BreakpointApplication.getByID(id)
    application match {
      case Some(application) => {
        Ok(views.html.applications.application(user, application))
      }
      case None => NotFound(views.html.error(
        "That application wasn't found.",
        "The application you attempted to view couldn't be found. Please check the URL and try your call again."))
    }
  }
}
