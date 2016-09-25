package controllers

import java.text.SimpleDateFormat
import java.util.Date

import play.api.libs.json._
import play.api.mvc.{Action, Controller}

import scala.util.Try

class CarAdvertController extends Controller {

  import controllers.DateUtil._

  //TODO: switch to DB storage lately.
  var cars = List[Car](
    Car(Some(22), "Audi A4", "disel", 10000, false, Option(43000), Option(strToDate("2008-09-14").get)),
    Car(Some(33), "Kia Ceed", "gasoline", 8000, false, Option(27000), Option(strToDate("2013-05-28").get)),
    Car(Some(777), "Skoda Octavia", "disel", 25000, true, None, None)
  )

  implicit val carAdWrites = new Writes[Car] {
    override def writes(c: Car): JsValue = Json.obj(
      "id" -> c.id,
      "title" -> c.title,
      "fuel" -> c.fuel,
      "price" -> c.price.toString,
      "new" -> c.isNew,
      "mileage" -> c.mileageStr,
      "first_registration" -> c.dateFirstReg
    )
  }

  implicit val adsWrites = new Writes[Adverts] {
    override def writes(adverts: Adverts): JsValue = Json.obj(
      "adverts" -> adverts.data
    )
  }

  def index = Action { request =>
    val json = Json.toJson(Adverts(cars))
    Ok(Json.stringify(json))
  }

  def read(id: Int) = Action { request =>
    cars.find(ad => ad.id == Option(id)) match {
      case Some(ad) =>
        val json = Json.toJson(ad)
        Ok(Json.stringify(json))

      case None => NoContent
    }
  }

  def create = TODO

  def update(id: Int) = TODO

  def delete(id: Int) = Action {request =>
    cars.find(c => c.id == Option(id)) match {
      case Some(car) =>
        cars = cars.filter(c => c.id != Option(id))
        Accepted

      case None => NotModified
    }
  }

  def describe = TODO
}

case class Car(id: Option[Int],
               title: String,
               fuel: String,
               price: Int,
               isNew: Boolean,
               mileage: Option[Int],
               firstReg: Option[Date]
              ) {

  def dateFirstReg: String = firstReg match {
    case Some(d) => DateUtil.dateToStr(d).getOrElse("")
    case None => ""
  }

  def mileageStr: String = mileage match {
    case Some (m) => m.toString
    case None => ""
  }
}

case class Adverts(data: Seq[Car])

object DateUtil {
  val formatter = new SimpleDateFormat("yyyy-mm-dd")

  def strToDate(dateStr: String): Try[Date] =
    Try(formatter.parse(dateStr))

  def dateToStr(date: Date): Try[String] =
    Try(formatter.format(date))
}
