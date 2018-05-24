package test.validator

import org. scalatest._
import scala.collection.immutable.List
import dripower.validate.BaseValidate
import dripower.validate._

class ValidateMethodSpec extends FlatSpec {

  "a transfromed trait should call real instance validator method" should "success" in {
    val d  = Demo(1,"123")
    d match {
      case bv: Demo => println(s"validate result -> ${bv.validator}")
      case _ => println("match trait failure")
    }
    assert(true)
  }

}

case class Demo(id: Int, name: String) extends BaseValidate {
  override def validator = {
    List(Validator.maxLength(name,4), Validator.biggerThan(id,3)).filter(r => r.isLeft)
  }

  def haha = {
    "123"
  }
}
