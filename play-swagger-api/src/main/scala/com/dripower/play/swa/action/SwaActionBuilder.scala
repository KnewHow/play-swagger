package com.dripower.play.swa

import play.api.mvc._
import play.api.http.Writeable
import scala.concurrent._

trait SwaAction[A, R] extends Action[A]

trait PostSwaAction[A,R] extends SwaAction[A,R]
trait GetSwaAction[A,R] extends SwaAction[A,R]

class SwaActionBuilder[Req[_], A] private(
  builder: ActionBuilder[Req, A]) {

  def async[A1, Res](_parser: BodyParser[A1])(body : Req[A1] => Future[Res])(
    implicit _writeable: Writeable[Res],
    _ec: ExecutionContext): SwaAction[A1, Res] = new SwaAction[A1, Res] {
    def executionContext = _ec
    def parser = _parser
    def apply(request: Request[A1]) = {
      builder.apply(parser).invokeBlock(request, (ra: Req[A1]) => body(ra).map(Results.Ok(_)))
    }
  }

  def asyncPost[A1, Res](_parser: BodyParser[A1])(body : Req[A1] => Future[Res])(
    implicit _writeable: Writeable[Res],
    _ec: ExecutionContext): PostSwaAction[A1, Res] = new PostSwaAction[A1, Res] {
    def executionContext = _ec
    def parser = _parser
    def apply(request: Request[A1]) = {
      builder.apply(parser).invokeBlock(request, (ra: Req[A1]) => body(ra).map(Results.Ok(_)))
    }
  }

  def asyncGet[A1, Res](_parser: BodyParser[A1])(body : Req[A1] => Future[Res])(
    implicit _writeable: Writeable[Res],
    _ec: ExecutionContext): GetSwaAction[A1, Res] = new GetSwaAction[A1, Res] {
    def executionContext = _ec
    def parser = _parser
    def apply(request: Request[A1]) = {
      builder.apply(parser).invokeBlock(request, (ra: Req[A1]) => body(ra).map(Results.Ok(_)))
    }
  }


  def async[Res](body: Req[A] => Future[Res])(implicit _writeable: Writeable[Res],
    _ec: ExecutionContext): SwaAction[A, Res] = async(builder.parser)(body)
}

object SwaActionBuilder {
  def apply[Req[_], A, Res](builder: ActionBuilder[Req, A]) = {
    new SwaActionBuilder(builder)
  }
}
