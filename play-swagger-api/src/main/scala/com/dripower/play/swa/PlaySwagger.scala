package play.swagger.api

import scala.language.experimental.macros
import shapeless._

object PlaySwagger {
  //定义宏
  def playApi[C]():List[String] =
    //宏实现
    macro Macros.api[C]


  def routes[T](a: T):Map[String, HNil => Any] =
    macro Macros.routes[T]

  def getAnnotation[C](): List[String] =
    macro Macros.obtainAnnotation[C]
}
