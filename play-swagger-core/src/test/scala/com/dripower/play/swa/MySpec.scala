package test.com.dripower.play.swa

import org. scalatest._

class MyTest extends FlatSpec {
  "A play swagger" should "return apis" in {
    val result = ExampleController.getApi()
    println("xixix")
    println("result" + result)
    assert(true)
  }
}
