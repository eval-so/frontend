package test

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import play.api.libs.ws.WS
import anorm._
import play.api.db._
import play.api.Play.current
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
        DB.withConnection { implicit c =>
          SQL("DELETE FROM USERS WHERE username={username}").on(
            'username -> "jsmith_integration_test"
          ).execute()
        }
        browser.goTo("http://localhost:3333/")
        browser.fill("#username").`with`("jsmith_integration_test")
        browser.fill("#email").`with`("jsmith@example.com")
        browser.fill("#password").`with`("my1337Passw0rd!")
        browser.submit("#register_form")
        User.getByUsername("jsmith_integration_test").get.confirm()
        browser.findFirst(".success").getText must contain("Welcome")
      }
    }

    "Allow a user to log in and be redirected to where they were going" in {
      running(TestServer(3333), HTMLUNIT) { browser =>
        browser.goTo("http://localhost:3333/applications/new")
        browser.fill("#username").`with`("jsmith_integration_test")
        browser.fill("#password").`with`("my1337Passw0rd!")
        browser.submit("#login_form")
        browser.url must equalTo("http://localhost:3333/applications/new")
      }
    }

    "Show a logged-in user the logged-in navbar" in {
      running(TestServer(3333), HTMLUNIT) { browser =>
        browser.goTo("http://localhost:3333/user/login")
        browser.fill("#username").`with`("jsmith_integration_test")
        browser.fill("#password").`with`("my1337Passw0rd!")
        browser.submit("#login_form")
        browser.pageSource must contain("Log Out")
      }
    }

    "Allow a logged-in user to create apps" in {
      running(TestServer(3333), HTMLUNIT) { browser =>
        browser.goTo("http://localhost:3333/applications/new")
        browser.fill("#username").`with`("jsmith_integration_test")
        browser.fill("#password").`with`("my1337Passw0rd!")
        browser.submit("#login_form")
        browser.title must equalTo("Create a New Application - Breakpoint")
      }
    }

    "Not allow a logged-out user to create apps" in {
      running(TestServer(3333), HTMLUNIT) { browser =>
        browser.goTo("http://localhost:3333/applications/new")
        browser.url must equalTo("http://localhost:3333/user/login")
      }
    }

  }
}
