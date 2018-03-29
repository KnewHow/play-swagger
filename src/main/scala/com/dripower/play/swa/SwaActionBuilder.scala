package com.dripower.play.swa

import play.api.mvc._
import play.api.http.Writeable
import scala.concurrent._

trait SwaAction[A, R] extends Action[A]

class SwaActionBuilder[Req[_], A, Res] private(
  builder: ActionBuilder[Req, A]
)(implicit _writeable: Writeable[Res], _ec: ExecutionContext) {

  def async(body : Req[A] => Future[Res]): SwaAction[A, Res] = new SwaAction[A, Res] {
    def executionContext = _ec
    def parser = builder.parser
    def apply(request: Request[A]) = {
      builder.invokeBlock(request, (ra: Req[A]) => body(ra).map(Results.Ok(_)))
    }
  }
}

object SwaActionBuilder {
  def apply[Req[_], A, Res](builder: ActionBuilder[Req, A])(implicit _writeable: Writeable[Res], _ec: ExecutionContext) = {
    new SwaActionBuilder(builder)
  }
}
