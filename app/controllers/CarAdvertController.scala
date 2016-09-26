package controllers

import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.{Inject, Singleton}

import play.api.libs.json._
import play.api.mvc.{Action, Controller}
import services.{AdvertStorage, IDGenerator}

import scala.util.{Failure, Success, Try}

@Singleton
class CarAdvertController @Inject()(idGenerator: IDGenerator, storage: AdvertStorage) extends Controller {

  import controllers.DateUtil._

  implicit val carAdWrites = new Writes[Car] {
    override def writes(c: Car): JsValue = Json.obj(
      "id" -> c.id,
      "title" -> c.title,
      "fuel" -> c.fuel.name,
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

  def show(orderBy: String) = Action { request =>
    val json = Json.toJson(Adverts(carsOrderedBy(orderBy, storage.find())))
    Ok(json)
  }

  def read(id: Int) = Action { request =>
    storage.read(id) match {
      case Some(ad) =>
        val json = Json.toJson(ad)
        Ok(json)

      case None => NoContent
    }
  }

  def create = Action { request =>
    request.body.asJson match {
      case Some(jsonBody) =>
        val car = parseJsonCar(jsonBody)
        if (car.isNew || validOldCar(car)) {
          storage.create(car)
          val msg = Json.toJson(Map("message" -> "added successfully"))
          Created(msg)
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
        val car = parseJsonCar(jsonBody, id)
        if (car.isNew || validOldCar(car)) {
          if (storage.update(car)) {
            val msg = Json.toJson(Map("message" -> "updated successfully"))
            Accepted(msg)
          } else {
            NotModified
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

  def delete(id: Int) = Action { request => if (storage.delete(id)) {
    val msg = Json.toJson(Map("message" -> "deleted successfully"))
    Accepted(msg)
  } else {
    NotModified
  }
  }

  def describe = Action { request =>
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
    Ok(msg)
  }

  private def parseJsonCar(json: JsValue, id: Int = idGenerator.generate()): Car = {
    val title = (json \ "title").as[String]
    val fuel = (json \ "fuel").as[String]
    val price = (json \ "price").as[Int]
    val isNew = (json \ "new").as[Boolean]
    val mileage = (json \ "mileage").validateOpt[Int].getOrElse(None)
    val firstReg: Option[Date] = parseFirstRegistration(json)

    Car(Option(id), title, Fuel(fuel), price, isNew, mileage, firstReg)
  }

  private def validOldCar(car: Car): Boolean = car.mileage.isDefined && car.firstReg.isDefined

  private def parseFirstRegistration(json: JsValue): Option[Date] = (json \ "first_registration").validateOpt[String].getOrElse(None) match {
    case Some(d) => strToDate(d) match {
      case s: Success[Date] => Option(s.get)
      case f: Failure[Date] => None
    }
    case None => None
  }

  private def carsOrderedBy(orderBy: String, data: List[Car]): List[Car] = orderBy match {
    case "title" => data.sortBy(_.title)
    case "fuel" => data.sortBy(_.id.getOrElse(0)).sortBy(_.fuel)
    case "price" => data.sortBy(_.price)
    case "new" => data.filter(c => c.isNew).sortBy(_.id.getOrElse(0)) ::: data.filter(c => !c.isNew).sortBy(_.id.getOrElse(0))
    case "mileage" => data.sortWith(_.mileage.getOrElse(0) < _.mileage.getOrElse(0))
    case "first_registration" => data.filter(c => c.firstReg.isDefined).sortBy(_.firstReg.get.getTime) ::: data.filter(c => !c.firstReg.isDefined).sortBy(_.id.getOrElse(0))
    case _ => data.sortBy(_.id.getOrElse(0))
  }
}

abstract class Fuel extends Ordered[Fuel] {

  override def compare(that: Fuel): Int = this.name compare that.name

  def name: String
}

object Fuel {
  def apply(name: String): Fuel = name match {
    case "diesel" => Diesel
    case _ => Gasoline
  }
}

case object Diesel extends Fuel {
  override def name: String = "diesel"
}

case object Gasoline extends Fuel {
  override def name: String = "gasoline"
}

case class Car(id: Option[Int],
               title: String,
               fuel: Fuel,
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
