package test

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import play.api.libs.ws.WS

import models._

class IntegrationTest extends Specification {
  "Frontend" should {
    "Show the front page" in {
      running(TestServer(3333)) {
        await(WS.url("http://localhost:3333/").get).status must equalTo(OK)
      }
    }

    "Allow a user to register" in {
      running(TestServer(3333), HTMLUNIT) { browser =>
        browser.goTo("http://localhost:3333/")
        browser.fill("#name").`with`("John Smith")
        browser.fill("#username").`with`("jsmith")
        browser.fill("#email").`with`("jsmith@example.com")
        browser.fill("#password").`with`("my1337Passw0rd!")
        browser.submit("#register_form")
        User.getByUsername("jsmith").get.confirm()
        browser.findFirst(".success").getText must contain("Welcome aboard")
      }
    }

    "Allow a user to log in" in {
      running(TestServer(3333), HTMLUNIT) { browser =>
        browser.goTo("http://localhost:3333/")
        browser.click("Log In")
        browser.fill("#username").`with`("jsmith")
        browser.fill("#password").`with`("my1337Passw0rd!")
        browser.submit("#login_form")
        browser.url must equalTo("http://localhost:3333/")
      }
    }
  }
}
