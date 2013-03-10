package com.daodecode.playtests

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import play.api.mvc._
import play.api.test.FakeApplication
import play.api.libs.concurrent.Promise

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

    override def isValid(ticket: String) = Promise.pure(ticket == "valid")
  }

  implicit def action2actionExecutor[A](wrapped: Action[(Action[A], A)]): ActionExecutor[A]
  = new ActionExecutor[A](wrapped)

  class ActionExecutor[A](wrapped: Action[(Action[A], A)]) {
    def process(request: Request[A]): Result = wrapped.parser(request).run.await.get match {
      case Left(errorResult) => errorResult
      case Right((innerAction, _)) => innerAction(request)
    }
  }

  "Any method wrapped in secured" should {

    "return UNAUTHORIZED if `authTicket` param is not provided" in {
      running(app) {
        val result = FakeController.securedAction process FakeRequest()
        status2(result) must_== UNAUTHORIZED
        contentAsString2(result) must_== "must be authenticated"
      }
    }

    def requestWithAuthTicket(ticket: String = "invalid") = FakeRequest().withHeaders("authTicket" -> ticket)

    "return UNAUTHORIZED if `authTicket` param is not valid" in {
      running(app) {
        val result = FakeController.securedAction process requestWithAuthTicket()
        status2(result) must_== UNAUTHORIZED
        contentAsString2(result) must_== "must be authenticated"
      }
    }

    "return whatever it returns if `authTicket` param is valid" in {
      running(app) {
        val result = FakeController.securedAction process requestWithAuthTicket(ticket = "valid")
        status2(result) must_== OK
        contentAsString2(result) must_== "Am I protected?"
      }
    }
  }

  "Any method NOT wrapped in secured" should {
    "return whatever it returns even without `authTicket` param " in {
      running(app) {
        val result = FakeController.nonSecuredAction(FakeRequest())
        status2(result) must_== OK
        contentAsString2(result) must_== "I don't care"
      }
    }
  }

  def status2(of: Result): Int = of match {
    case Result(status, _) => status
    case AsyncResult(promise) => status2(await(promise))
  }

  def contentAsString2(of: Result): String = new String(contentAsBytes2(of), "utf-8")

  def contentAsBytes2(of: Result): Array[Byte] = of match {
    case AsyncResult(promise) => contentAsBytes(await(promise))
    case _ => contentAsBytes(of)
  }

  val app = FakeApplication()
}
