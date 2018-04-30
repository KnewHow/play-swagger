package test.com.dripower.play.swa

import akka.util.ByteString
import play.api.mvc._
import play.api.libs.json._
import play.api.http.Writeable
import scala.concurrent._
import com.dripower.play.swa._
import org.joda.time._
import org. scalatest._


case class Car(id:Long,logo:String)
case class PersonGet(id: Long)
case class Person(id: Long, name: String, age: Int,car: Car,level1ExpiredAt: Option[DateTime] = None,avatar: Option[String] = None,gmtCreate:DateTime = DateTime.now(),gmtModified:DateTime = DateTime.now(),friends:List[Friend] = List())

case class Friend(id:Long,name:String,relation:String)
case class Ids(ids:List[Long],op:Option[String])

object PersonGet {
  implicit val format = Json.format[PersonGet]
}

object Ids {
  implicit val format = Json.format[Ids]
}


object Person {
  implicit val personWriteable = Writeable((p: Person) => ByteString(p.toString), Some("text/plain"))

  implicit val personListWriteable = Writeable((ps: List[Person]) => ByteString(ps.toString), Some("text/plain"))
}

class ExampleController(val controllerComponents: ControllerComponents)(implicit ec: ExecutionContext) extends BaseController {

  val Swa = SwaActionBuilder(Action)

  def exampleAction: SwaAction[PersonGet, Person] = Swa.async[PersonGet,Person](parse.json[PersonGet]) { req =>
    val personGet = req.body

    Future.successful(Person(personGet.id, "foo", 1,Car(1L,"奔驰")))
  }

  def examplePostAction:PostSwaAction [PersonGet, Person] = Swa.asyncPost[PersonGet,Person](parse.json[PersonGet]) { req =>
    val personGet = req.body
    Future.successful(Person(personGet.id, "foo", 1,Car(1L,"奔驰")))
  }

   def exampleGetAction:GetSwaAction[PersonGet, List[Person]] = Swa.asyncGet[PersonGet,List[Person]](parse.json[PersonGet]) { req =>
    val personGet = req.body
    Future.successful(List(Person(personGet.id, "foo", 1,Car(1L,"奔驰"))))
   }

  // def getPersonLists:GetSwaAction[Ids,List[Person]] = Swa.asyncGet[Ids,List[Person]](parse.json[Ids]) { req =>
  //   val ids = req.body.ids
  //   import scala.collection.mutable.ListBuffer
  //   val results = new ListBuffer[Person]()
  //   for(id <- ids) {
  //     results += (Person(id, "foo", 1,Car(1L,"奔驰")))
  //   }
  //   Future.successful(results.toList)
  // }
}



object  ExampleController extends FlatSpec{
  def getApi() = {
    import swagger.api._
    PlaySwagger.playApi[ExampleController]()
  }
}
