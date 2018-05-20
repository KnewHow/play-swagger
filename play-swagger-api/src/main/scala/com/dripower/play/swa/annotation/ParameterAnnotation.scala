package play.swagger.annotation

import scala.annotation.Annotation
/**
 *  参数的注解，作用于参数的 case class 上面
 */
final class ParameterAnnotation(
  descrip: Map[String, String]
) extends Annotation
