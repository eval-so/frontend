package controllers

import so.eval.{EvaluationRequest, Router}
import so.eval.SandboxedLanguage.Result

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.concurrent.Akka
import play.api.libs.functional.syntax._
import play.api.Play.current
import play.modules.statsd.api.Statsd

import scala.concurrent.ExecutionContext.Implicits.global

object Application extends Controller {

  /** Return an Action with CORS headers set. */
  private def CORSAction[A](bp: BodyParser[A])(f: Request[A] => play.api.mvc.Result) =
    Action(bp) { request =>
      f(request).withHeaders("Access-Control-Allow-Origin" -> "*")
    }

  /** Return an Action with CORS headers set. */
  private def CORSAction(f: Request[AnyContent] => play.api.mvc.Result) =
    Action { request =>
      f(request).withHeaders("Access-Control-Allow-Origin" -> "*")
    }

  def CORSPreflight(path: String) = Action {
    Ok.withHeaders(
      "Access-Control-Allow-Origin" -> "*",
      "Access-Control-Allow-Methods" -> "POST",
      "Access-Control-Max-Age" -> "300",
      "Access-Control-Allow-Headers" -> "Origin, X-Requested-With, Content-Type, Accept"
    )
  }

  def index = Action {
    Ok(views.html.index())
  }

  def apiDocs(version: Int) = Action { Ok(views.html.apidocs.v1()) }

  def tryEvalSO = Action { Ok(views.html.tryEvalSO()) }

  def languages(version: Int) = CORSAction { request =>
    val languages = Router.languages.keys.map { shortname =>
      (shortname -> Router.displayName(shortname).getOrElse(""))
    }.toMap
    Ok(Json.toJson(languages))
  }

  case class Evaluation(language: String, code: String)

  implicit val rds = (
    (__ \ 'language).read[String] and
    (__ \ 'code).read[String] and
    (__ \ 'inputFiles).readNullable[Map[String, String]] and
    (__ \ 'compilationOnly).readNullable[Boolean] and
    (__ \ 'stdin).readNullable[String]
  ) tupled

  implicit val evaluationWrites = Json.writes[Result]

  def evaluate(version: Int) = CORSAction(parse.json) { request =>
    request.body.validate[(String, String, Option[Map[String, String]], Option[Boolean], Option[String])].map {
      case (language, code, inputFiles, compilationOnly, stdin) => {
        val evaluationRequest = EvaluationRequest(
          code,
          files = inputFiles,
          compilationOnly = compilationOnly.getOrElse(false),
          stdin = stdin)
        val evaluation = Router.route(language, evaluationRequest)

        // Graphite doesn't seem to be handling names with "+" in it.
        // If we have to do more of these replacements, we should move them out
        // to somewhere more global (like minibcs).
        // But for now, only Frontend provides language metrics, and we can
        // handle the one-off case here.
        val sanitizedLanguage = language.replaceAll("\\+", "plus")

        evaluation match {
          case Some(sandbox) => {
            val evalPromise = Akka.future { sandbox.evaluate }
            Async {
              evalPromise.map {
                _.map { resultTry =>
                  resultTry.compilationResult match {
                    case Some(result) => Statsd.timing(s"evaluation.${sanitizedLanguage}.compilation.walltime", result.wallTime)
                    case _ =>
                  }
                  Statsd.timing(s"evaluation.${sanitizedLanguage}.execution.walltime", resultTry.wallTime)
                  Statsd.increment(s"evaluation.${sanitizedLanguage}.ok", value = 1)
                  Ok(Json.toJson(resultTry))
                }.getOrElse {
                  Statsd.increment(s"evaluation.${sanitizedLanguage}.error", value = 1)
                  BadRequest(Json.obj("error" -> "An error has occurred and evaluation has halted."))
                }
              }
            }
          }
          case None => BadRequest(Json.obj("error" -> "No such language."))
        }
      }
    }.recoverTotal{
      e => BadRequest(Json.obj("error" -> JsError.toFlatJson(e)))
    }
  }
}
