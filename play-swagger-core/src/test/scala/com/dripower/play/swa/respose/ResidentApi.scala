package test.resident

import akka.util.ByteString
import play.api.mvc._
import javax.inject._
import play.api.libs.json._
import play.api.http.Writeable
import scala.concurrent._
import com.dripower.play.swa._
import org.joda.time._
import play.swagger.annotation._
import scala.annotation.meta
import dripower.validate._
import scala.collection.immutable.List
import scala.util._
import play.swagger.response._
import play.swagger.api._

case class ResidentGet(name: String)
case class Resident(name: String, age: Int, role: Option[String])

object ResidentGet {
  implicit val format = Json.format[ResidentGet]
}
object Resident {

  implicit val residentWrites = Json.writes[Resident]

  implicit val residentWriteable = Writeable((r: Resident) =>
    ByteString(
      Json.stringify(Json.toJson(r))
    ), Some("application/json")
  )

  implicit val residentResBodyWriteable = Writeable( (r:ResBody[Resident]) =>
    ByteString(
      Json.stringify(
        Json.obj(
          "code" -> r.code,
          "data" -> Json.toJson(r.data)
        )
      )
    ), Some("application/json")
  )
  // implicit val residentWriteable = Json.writes[Resident]
}
class ResidentApi @Inject() (val controllerComponents : ControllerComponents)(implicit ec: ExecutionContext) extends BaseController  {
  val Swa = SwaActionBuilder(Action)

  @ActionAnnotation("获取居民信息")
  def get:PostSwaAction[ResidentGet, ResBody[Resident]] = Swa.asyncPost[ResidentGet, ResBody[Resident]](parse.json[ResidentGet]) {req =>
    val rg = req.body
    val r = ResBody[Resident](
      200,
      Resident(rg.name, 12, Option("teacher"))
    )
    Future.successful(
      r
    )
  }
}

object ResidentApi {
  def api = {
    PlaySwagger.playApi[ResidentApi]()
  }
}
