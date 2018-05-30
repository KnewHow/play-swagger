package play.swagger.response

case class ResBody[T] (
  code: Int,
  data: T
)
