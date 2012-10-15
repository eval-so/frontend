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
          SQL(
            """delete from application_users where
            user_id = (select id from users where username={username})"""
          ).on(
            'username -> "jsmith_integration_test"
          ).execute()

          SQL("DELETE FROM users WHERE username={username}").on(
            'username -> "jsmith_integration_test"
          ).execute()
        }
        browser.goTo("http://localhost:3333/")
        browser.fill("#username").`with`("jsmith_integration_test")
        browser.fill("#email").`with`("jsmith@example.com")
        browser.fill("#password").`with`("my1337Passw0rd!")
        browser.submit("#register_form")
        User.getByUsername("jsmith_integration_test").get.confirm()
        browser.findFirst(".success").getText must contain("Welcome aboard")
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
        DB.withConnection { implicit c =>
          SQL(
            """delete from application_users where
            application_id = (select id from applications where name={name})"""
          ).on(
            'name -> "Test app 1"
          ).execute()

          SQL(
            """delete from applications where name={name}"""
          ).on(
            'name -> "Test app 1"
          ).execute()
        }
        browser.goTo("http://localhost:3333/applications/new")
        browser.fill("#username").`with`("jsmith_integration_test")
        browser.fill("#password").`with`("my1337Passw0rd!")
        browser.submit("#login_form")
        browser.title must equalTo("Create a New Application - Breakpoint")
        browser.fill("#name").`with`("Test app 1")
        browser.fill("#description").`with`("Made by the Frontend test suite.")
        browser.click("#allow_anonymous_auth_true")
        browser.submit("#new_application_form")
        browser.url must beMatching(""".*applications/\d+""")
        browser.title must contain("Viewing Application")
      }
    }

    "Allow a logged-in user to edit apps that they own (only)" in {
      running(TestServer(3333), HTMLUNIT) { browser =>
        browser.goTo("http://localhost:3333/applications/1/edit")
        browser.fill("#username").`with`("jsmith_integration_test")
        browser.fill("#password").`with`("my1337Passw0rd!")
        browser.submit("#login_form")
        println(browser.pageSource)
        browser.title must equalTo("Edit Application - Breakpoint")
        browser.fill("#name").`with`("Test app edited")
        browser.fill("#description").`with`("Edited by the Frontend test suite.")
        browser.submit("#new_application_form")
        browser.findFirst("#name").getText must equalTo("Test app edited")

        browser.goTo("http://localhost:3333/applications/1337/edit")
        browser.pageSource must contain("You don't appear to be an owner")
      }
    }

    "Allow a logged-in user to update their secret key" in {
      running(TestServer(3333), HTMLUNIT) { browser =>
        browser.goTo("http://localhost:3333/user/update_profile")
        browser.fill("#username").`with`("jsmith_integration_test")
        browser.fill("#password").`with`("my1337Passw0rd!")
        browser.submit("#login_form")
        browser.title must equalTo("My Profile - Breakpoint")
        val oldKey = browser.findFirst("#secret_key").getText
        browser.submit("#change_secret_key_form")
        browser.findFirst("#secret_key").getText must not equalTo(oldKey)
        browser.url must equalTo("http://localhost:3333/user/update_profile")
      }
    }

    "Allow a logged-in user to edit their profile" in {
      running(TestServer(3333), HTMLUNIT) { browser =>
        browser.goTo("http://localhost:3333/user/update_profile")
        browser.fill("#username").`with`("jsmith_integration_test")
        browser.fill("#password").`with`("my1337Passw0rd!")
        browser.submit("#login_form")
        browser.title must equalTo("My Profile - Breakpoint")
        browser.fill("#name").`with`("John Smith")
        browser.fill("#new_password").`with`("newPassw0rd13373R")
        browser.fill("#old_password").`with`("my1337Passw0rd!")
        browser.submit("#user_form")
        browser.url must equalTo("http://localhost:3333/user/update_profile")
        browser.findFirst("#name").getValue must equalTo("John Smith")
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
