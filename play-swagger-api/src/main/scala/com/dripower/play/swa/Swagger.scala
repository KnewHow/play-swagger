package swagger.api

import scala.language.experimental.macros

object PlaySwagger {
  def playApi[C](a:Any):List[String] =
    macro Macros.api[C]
}
