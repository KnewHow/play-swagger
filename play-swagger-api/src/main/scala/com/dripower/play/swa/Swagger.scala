package swagger.api

import scala.language.experimental.macros

object PlaySwagger {
  def playApi[C](a:Any) =
    macro Macros.api[C]
}
