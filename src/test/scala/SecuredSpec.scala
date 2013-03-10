package com.daodecode.playtests

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import play.api.mvc._
import play.api.test.FakeApplication
import play.api.libs.concurrent.Promise
import concurrent.duration.Duration

class SecuredSpec extends Specification {

  object FakeController extends Controller with Secured {
    def securedAction = secured {
      Action {
        request => Ok("Am I protected?")
      }
    }

    def nonSecuredAction = Action {
      request => Ok("I don't care")
    }

    def isValid(ticket: String) = Promise.pure(ticket == "valid")
  }

  "Any method wrapped in secured" should {

    "return UNAUTHORIZED if `authTicket` param is not provided" in {
      running(app) {
        val result = FakeController.securedAction process FakeRequest()
        status(result) must_== UNAUTHORIZED
        contentAsString(result) must_== "must be authenticated"
      }
    }

    def requestWithAuthTicket(ticket: String = "invalid") = FakeRequest().withHeaders("authTicket" -> ticket)

    "return UNAUTHORIZED if `authTicket` param is not valid" in {
      running(app) {
        val result = FakeController.securedAction process requestWithAuthTicket()
        status(result) must_== UNAUTHORIZED
        contentAsString(result) must_== "must be authenticated"
      }
    }

    "return whatever it returns if `authTicket` param is valid" in {
      running(app) {
        val result = FakeController.securedAction process requestWithAuthTicket(ticket = "valid")
        status(result) must_== OK
        contentAsString(result) must_== "Am I protected?"
      }
    }
  }

  "Any method NOT wrapped in secured" should {
    "return whatever it returns even without `authTicket` param " in {
      running(app) {
        val result = FakeController.nonSecuredAction(FakeRequest())
        status(result) must_== OK
        contentAsString(result) must_== "I don't care"
      }
    }
  }

  implicit class ActionExecutor(action: EssentialAction) {
    def process[A](request: Request[A]): Result =
      concurrent.Await.result(action(request).run, Duration(1, "sec"))
  }

  val app = FakeApplication()
}
