package dripower.validate

import scala.util.Either
trait BaseValidate {
  def validator: List[Either[String, Boolean]]
}
