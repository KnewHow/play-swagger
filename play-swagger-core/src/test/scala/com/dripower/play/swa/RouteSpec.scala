package play.swagger.test

import org. scalatest._
import play.swagger.api.PlaySwagger
import demo._
import shapeless._

class RouteSpec extends FlatSpec {
  "A route macro" should "success" in {
    val c = new DemoController
    val m = PlaySwagger.routes[DemoController](c)
    var lamda = m.get("test-DemoController-demoAction")
    println(s"lamda -> ${lamda}")
    lamda match {
      case Some(r) => r(HNil)
      case _ => println("the method is not exsit")
    }
    assert(true)
  }
}

class DemoController {
  def demoAction = {
    println(s"lala -> ")
  }
}
