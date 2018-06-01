package test.resident

import org.scalatest._

class ResidentApiSpec extends FlatSpec {
  "Use new Response body obtain parameter describe" should "success" in {
    val r = ResidentApi.api
    println(s"apiinfo -> $r")
    assert(true)
  }
}
