package play.swagger.api

import scala.language.experimental.macros

object PlaySwagger {
  def playApi[C](a:Any):List[String] =
    macro Macros.api[C]


  def routes[T](a: T):Map[String,Any] =
    macro Macros.routes[T]
}
