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

import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{ Failure, Try, Success }

object Application extends Controller {

  implicit val timeout: Timeout = Timeout(20.seconds)
  val router = Akka.system.actorOf(Props[Router], name = "router")

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

          // Graphite doesn't seem to be handling names with "+" in it.
          // If we have to do more of these replacements, we should move them out
          // to somewhere more global (like minibcs).
          // But for now, only Frontend provides language metrics, and we can
          // handle the one-off case here.
          val sanitizedLanguage = language.replaceAll("\\+", "plus")

          Router.route(language, evaluationRequest) match {
            case None => {
              Statsd.increment(s"evaluation.${sanitizedLanguage}.does-not-exist", value = 1)
              BadRequest(Json.obj("error" -> "No such language."))
            }

            case Some(routed) => {
              val future = router ? routed
              Async {
                future.map { tryResult =>
                  tryResult.asInstanceOf[Try[so.eval.SandboxedLanguage.Result]] match {
                    case Success(result) => {
                      result.compilationResult match {
                        case Some(result) =>
                          Statsd.timing(s"evaluation.${sanitizedLanguage}.compilation.walltime", result.wallTime)
                        case _ =>
                      }

                      Statsd.timing(s"evaluation.${sanitizedLanguage}.execution.walltime", result.wallTime)
                      Statsd.increment(s"evaluation.${sanitizedLanguage}.ok", value = 1)
                      Ok(Json.toJson(result))
                    }
                    case Failure(result) => { // TODO: Log result.getMessage.
                      Statsd.increment(s"evaluation.${sanitizedLanguage}.error", value = 1)
                      BadRequest(Json.obj("error" -> "An error has occurred and evaluation has halted."))
                    }
                  }
                }
              }
            }
          }
      }
    } recoverTotal {
      e => {
        Statsd.increment(s"json.error.validation", value = 1)
        BadRequest(Json.obj("error" -> JsError.toFlatJson(e)))
      }
    }
  }
}
