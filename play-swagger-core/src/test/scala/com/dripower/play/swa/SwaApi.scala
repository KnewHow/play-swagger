package demo2

import akka.util.ByteString
import play.api.mvc._
import play.api.libs.json._
import play.api.http.Writeable
import scala.concurrent._
import org.joda.time._
import com.dripower.play.swa._
import org.scalatest._
import play.swagger.annotation._
import scala.annotation.meta
import play.swagger.api._

case class PersonGet(
  @(FieldAnnotation @meta.getter)("人的ID")
    id: Long
)
case class Person(
  @(FieldAnnotation @meta.getter)("人的ID")
    id: Long,
  @(FieldAnnotation @meta.getter)("人的名字")
    name: String,
  @(FieldAnnotation @meta.getter)("人的年龄")
    age: Int,
  @(FieldAnnotation @meta.getter)("人的头像")
    avatar: Option[String] = None,
)

object PersonGet {
  implicit val format = Json.format[PersonGet]
}

object Person {
  implicit val personWriteable = Writeable((p: Person) => ByteString(p.toString), Some("text/plain"))

}


class ShowController(val controllerComponents: ControllerComponents)(implicit ec: ExecutionContext) extends BaseController {
  val Swa = SwaActionBuilder(Action)

  @ActionAnnotation(descrip="测试 POST 请求")
  def examplePostAction: PostSwaAction [PersonGet, Person] = Swa.asyncPost[PersonGet,Person](parse.json[PersonGet]) { req =>
    val personGet = req.body
    Future.successful(Person(personGet.id, "foo", 1))
  }

}
object ExampleControllerApi {
  def getApi = {
    // val r = PlaySwagger.playApi[ShowController]()
    // r
    ""
  }

}
