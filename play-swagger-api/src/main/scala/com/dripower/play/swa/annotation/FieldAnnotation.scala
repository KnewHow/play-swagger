package play.swagger.annotation

import scala.annotation.Annotation

/**
 * 参数是注解，作用于参数的 case class 字段
 */
final class FieldAnnotation(
  descrip: String
) extends Annotation
