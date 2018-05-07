package play.swagger.route.api

case class PlayRoute (
  method:String,
  requestPath:String
)

object PlayRoute {
  final val POST = "POST"
  final val GET  = "GET"
}
