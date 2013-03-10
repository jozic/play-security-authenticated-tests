package com.daodecode.playtests

import play.api.mvc._
import play.api.mvc.Results._
import play.api.libs.ws.WS
import play.api.Play.current
import play.api.libs.concurrent.Promise
import play.api.Logger

trait Secured {

  val logger = Logger("secured")

  final def fail(reason: String) = {
    logger.debug("Access attempt failed: " + reason)
    Unauthorized("must be authenticated")
  }

  final def secured[A](action: Action[A]) =
    Security.Authenticated(
      req => req.headers.get("authTicket"), 
      _ => fail("no ticket found")) {
      ticket => Action(action.parser) {
        request => withTicket(ticket) {
          action(request)
        }
      }
    }

  private def withTicket(ticket: String)(produceResult: => Result): Result =
    Async {
      isValid(ticket) map {
        valid => if (valid) produceResult else fail("provided ticket %s is invalid".format(ticket))
      }
    }

  def isValid(ticket: String): Promise[Boolean]
}
