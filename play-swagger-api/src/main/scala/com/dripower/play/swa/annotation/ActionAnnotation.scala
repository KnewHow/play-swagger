package play.swagger.annotation

import scala.annotation.Annotation
/**
 * action 注解，作用于 Action,用于描述接口信息
 */
final class ActionAnnotation(
  descrip: String // 描述Action
) extends Annotation
