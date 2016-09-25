package controllers

import java.text.SimpleDateFormat
import java.util.Date

import play.api.libs.json._
import play.api.mvc.{Action, Controller}

import scala.util.{Failure, Success, Try}

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

  def create = Action { request =>
    request.body.asJson match {
      case Some(jsonBody) =>
        val car = parseJsonCar(jsonBody)
        if (car.isNew || validOldCar(car)) {
          cars = cars ::: List(car)
          Created
        } else {
          val msg = Json.toJson(Map("message" -> "`new` and `first_registration` are mandatory for old cars."))
          BadRequest(msg)
        }

      case None =>
        val msg = Json.toJson(Map("message" -> "Expected application/json request."))
        BadRequest(msg)
    }
  }

  def update(id: Int) = Action { request =>
    request.body.asJson match {
      case Some(jsonBody) =>
        val car = parseJsonCar(jsonBody)
        if (car.isNew || validOldCar(car)) {
          cars.find(c => c.id == Option(id)) match {
            case Some(carForUpdate) =>
              cars = cars.filter(c => c.id != Option(id)) ::: List(
                carForUpdate.copy(
                  title = car.title, fuel = car.fuel,
                  price = car.price, isNew = car.isNew,
                  mileage = car.mileage, firstReg = car.firstReg
                )
              )
              Accepted

            case None => NotModified
          }
        } else {
          val msg = Json.toJson(Map("message" -> "`new` and `first_registration` are mandatory for old cars."))
          BadRequest(msg)
        }
      case None =>
        val msg = Json.toJson(Map("message" -> "Expected application/json request."))
        BadRequest(msg)
    }
  }

  def delete(id: Int) = Action { request =>
    cars.find(c => c.id == Option(id)) match {
      case Some(car) =>
        cars = cars.filter(c => c.id != Option(id))
        Accepted

      case None => NotModified
    }
  }

  def describe = Action{ request =>
    val msg = Json.obj(
      "message" -> "You may use the next URIs to interact with car advert service",
      "desc" -> Json.arr(
        Json.obj(
          "method" -> "GET",
          "uri" -> "/api/car-ads",
          "explanation" -> "returns list of all car adverts"
        ),
        Json.obj(
          "method" -> "GET",
          "uri" -> "/api/car-ad/:id",
          "explanation" -> "returns data for single car advert by id"
        ),
        Json.obj(
          "method" -> "PUT",
          "uri" -> "/api/car-ad",
          "explanation" -> "adds car advert"
        ),
        Json.obj(
          "method" -> "POST",
          "uri" -> "/api/car-ad/:id",
          "explanation" -> "modifys car advert by id"
        ),
        Json.obj(
          "method" -> "DELETE",
          "uri" -> "/api/car-ad/:id",
          "explanation" -> "deletes car advert by id"
        ),
        Json.obj(
          "method" -> "OPTIONS",
          "uri" -> "/api/car-ads ",
          "explanation" -> "provides available URIs and its description"
        )
      )
    )
    Ok (Json.stringify(msg))
  }

  private def parseJsonCar(json: JsValue): Car = {
    val title = (json \ "title").as[String]
    val fuel = (json \ "fuel").as[String]
    val price = (json \ "price").as[Int]
    val isNew = (json \ "new").as[Boolean]
    val mileage = (json \ "mileage").validateOpt[Int].getOrElse(None)
    val firstReg: Option[Date] = parseFirstRegistation(json)

    Car(Option(999), title, fuel, price, isNew, mileage, firstReg)
  }

  private def validOldCar(car: Car): Boolean = car.mileage.isDefined && car.firstReg.isDefined

  private def parseFirstRegistation(json: JsValue): Option[Date] = (json \ "first_registration").validateOpt[String].getOrElse(None) match {
    case Some(d) => strToDate(d) match {
      case s: Success[Date] => Option(s.get)
      case f: Failure[Date] => None
    }
    case None => None
  }
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
    case Some(m) => m.toString
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
