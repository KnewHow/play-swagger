package dripower.validate

import scala.util.Either
import scala.util.Right
import scala.util.Left

object Validator {
  def maxLength(s: String, maxL: Int, msg: Option[String] = None):Either[String, Boolean] = {
    if (s.length > maxL) {
      msg match {
        case Some(m) => Left(m)
        case None    => Left(s"字符串:${s}的最大长度不允许超过:${maxL}")
      }
    } else {
      Right(true)
    }
  }

  def biggerThan(n: Int, max: Int, msg: Option[String]=None): Either[String, Boolean] = {
    if (n > max) {
      msg match {
        case Some(m) => Left(m)
        case None    => Left(s"${n}不能大于:${max}")
      }
    } else {
      Right(true)
    }
  }
}
