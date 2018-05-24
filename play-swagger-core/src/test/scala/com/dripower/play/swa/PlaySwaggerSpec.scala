package demo2

import org. scalatest._
import play.swagger.api.PlaySwagger
class PlaySwaggerSpec extends FlatSpec {
  "A play swagger" should "return apis" in {
    val result = ExampleControllerApi.getApi
    println(s"router ->\n $result")
    assert(true)
  }
}
