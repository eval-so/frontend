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
import play.api.libs.ws.WS
import scala.concurrent.ExecutionContext.Implicits.global
import org.jsoup.Jsoup

/** Controllers to handle all calls to version 1 of the Breakpoint API. */
object APIv1Controller extends Controller {

  /** Use GitHub wiki for API documentation, because we're Web Scale!
    *
    * We pass users off to the breakpoint-eval/frontend wiki on GitHub,
    * where the community can document the API and add examples and do
    * all sorts of cool things.
    *
    * We pull in jsoup here and do evil, nasty things.
    *
    * We use the Play WS system, so that we can do futures/promises properly.
    * Then, we take the body of the response, throw it to Jsoup and parse out
    * the title. If the title is the "Home" page of the wiki, then we've been
    * redirected back off of the wiki page, meaning the wiki page likely
    * doesn't exist and we don't have permission to create it.
    *
    * We will never have permission (as long as GitHub's workflow doesn't
    * change) to edit the page, because internally, we never authenticate to
    * GitHub, and unauthenticated users can't create new pages.
    *
    * @note This is experimental and might vanish.
    * @param method The method (or path to the method) being documented.
    */
  def document(method: String) = Action { implicit request =>
    val wikiMethod = "v1-" + method.replace("/", "-")
    Async {
      val wiki = WS.url("https://github.com/breakpoint-eval/frontend/wiki/" + wikiMethod)
      wiki.get map { response =>
        val html = Jsoup.parse(response.body)
        if (html.title == "Home · breakpoint-eval/frontend Wiki · GitHub") {
          NotFound(views.html.error(
            "That method doesn't seem to be documented.",
            "The method you're looking up doesn't seem to be documented. If this seems like a legitimate error, please let us know by reading below and taking the appropriate action."))
        } else {
          Redirect(wiki.url)
        }
      }
    }
  }

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
