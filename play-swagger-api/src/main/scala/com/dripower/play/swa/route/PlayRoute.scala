package com.dripower.play.route

case class PlayRoute(
  method:String,
  requestPath:String,
  actionPath:String
)

object PlayRoute {
  final val GET: String = "GET"
  final val POST: String = "POST"
  def writeIntoFile(routes:List[PlayRoute],path:String) = ???
}
