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

package breakpoint.util
import play.api._
import play.api.Play.current
import play.api.Configuration._
import org.apache.commons.mail._
import scala.collection.JavaConversions._

object BreakpointEmail {
  val smtpHost = Play.application.configuration.getString("smtp.host").getOrElse {
    throw new RuntimeException("smtp.host needs to be set in application.conf in order to use this plugin (or set smtp.mock to true)")
  }
  val smtpPort = Play.application.configuration.getInt("smtp.port").getOrElse(25)
  val smtpSsl = Play.application.configuration.getBoolean("smtp.ssl").getOrElse(false)
  val smtpUser = Play.application.configuration.getString("smtp.user")
  val smtpPassword = Play.application.configuration.getString("smtp.password")

  /** Send an email using Apache Commons Mailer.
    *
    * Synchronously send an email to a given address using the Apache
    * Commons mailer library.
    *
    * Assumes UTF-8 in all cases.
    *
    * @param to The address to send the message to.
    * @param subject The subject of the message.
    * @param body The message's contents.
    * @param from The address to send the message from.
    */
  def send(
    to: String,
    subject: String,
    body: String,
    from: String = "Breakpoint <noreply@breakpoint-eval.org>"
  ): Either[EmailException, String] = {
    val email = new MultiPartEmail()
    email.setHostName(smtpHost)
    email.setSmtpPort(smtpPort)
    email.setSSL(smtpSsl)
    email.setFrom(from)
    email.addTo(to)
    email.setSubject(subject)
    email.setMsg(body)
    try {
      Right(email.send())
    } catch {
      case e: EmailException => Left(e)
    }
  }
}
