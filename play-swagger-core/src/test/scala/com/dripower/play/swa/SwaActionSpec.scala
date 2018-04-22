package test.com.dripower.play.swa

import akka.util.ByteString
import play.api.mvc._
import play.api.libs.json._
import play.api.http.Writeable
import scala.concurrent._
import com.dripower.play.swa._

case class PersonGet(id: Long)
case class Person(id: Long, name: String, age: Int)

object PersonGet {
  implicit val format = Json.format[PersonGet]
}

object Person {
  implicit val personWriteable = Writeable((p: Person) => ByteString(p.toString), Some("text/plain"))
}

class ExampleController(val controllerComponents: ControllerComponents)(implicit ec: ExecutionContext) extends BaseController {

  val Swa = SwaActionBuilder(Action)

  def exampleAction: SwaAction[PersonGet, Person] = Swa.async[PersonGet,Person](parse.json[PersonGet]) { req =>
    val personGet = req.body
    Future.successful(Person(personGet.id, "foo", 1))
  }
}

object  ExampleController {
  import swagger.api._
  PlaySwagger.playApi[ExampleController]()
}
