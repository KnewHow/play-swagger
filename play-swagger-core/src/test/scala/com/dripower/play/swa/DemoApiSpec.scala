package test.com.dripower.play.swa

import org. scalatest._
import demo._

class DemoApiSpec extends FlatSpec {
  "test obtain DemoApi API document data" should "suceess" in {
    val a = DemoController.getApi
    println(s"api -> ${a}")
    assert(true)
  }
}
