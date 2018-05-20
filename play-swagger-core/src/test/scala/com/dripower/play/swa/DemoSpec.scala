package test.com.dripower.play.swa

import org. scalatest._
import demo._

class DemoSpec extends FlatSpec {
  "A Demo test" should "suceess" in {
    val a = DemoController.getApi
    println(s"aaaaaaaaa -> ${a}")
    assert(true)
  }
}
