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

import play.api._
import play.api.mvc._
import play.api.libs.json.Json._

/** Controllers to handle all calls to version 1 of the Breakpoint API. */
object APIv1Controller extends Controller {

  /** The evaluation controller.
    *
    * Lets users evaluate code by POSTing to here.
    * 
    * We take the request, do some sanity checking, then pass it down stream to
    * the AS.
    */
  def evaluate = Action(parse.json) { implicit request =>
    Ok(toJson(
      Map("this_works" -> true)
    ))
  }
}
