package controllers

import org.scalatestplus.play._
import play.api.libs.json.Json
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test._

import scala.concurrent.Future

class CarAdvertControllerSpec extends PlaySpec with Results {

  val controller = new CarAdvertController()

  "Car Advert #index" must {
    "return list of all car adverts" in {
      val result: Future[Result] = controller.index()
        .apply(FakeRequest(GET, "/api/car-ads"))
      val bodyText: String = contentAsString(result)
      val json = Json.parse(bodyText)

      val titles = (json \ "adverts" \\ "title")
        .map(_.as[String]) //to convert JsString to String

      val isNews = (json \ "adverts" \\ "new")
        .map(_.as[Boolean])

      titles must ===(
        List("Audi A4", "Kia Ceed", "Skoda Octavia")
      )

      isNews must ===(List(false, false, true))
    }
  }

  "Car Advert #read(id)" must {
    "return data for single car advert by id" in {
      val result: Future[Result] = controller.read(777)
        .apply(FakeRequest(GET, "/api/car-ad/777"))
      val bodyText: String = contentAsString(result)
      val json = Json.parse(bodyText)

      (json \ "id").as[Int] must be(777)
      (json \ "title").as[String] must be("Skoda Octavia")
      (json \ "fuel").as[String] must be("disel")
      (json \ "new").as[Boolean] must be(true)
      (json \ "mileage").as[String] must be("")
      (json \ "first_registration").as[String] must be("")
    }

    "return `No Content` for single car advert if no data was" +
      "found for the specified id" in {
      val result: Future[Result] = controller.read(111)
        .apply(FakeRequest(GET, "/api/car-ad/111"))

      status(result) must be (204)
    }
  }

  "Car Advert #delete(id)" must {
    "delete car advert by id" in {
      val result: Future[Result] = controller.delete(777)
        .apply(FakeRequest(GET, "/api/car-ad/777"))

      val afterDeleteResult: Future[Result] = controller.index()
        .apply(FakeRequest(GET, "/api/car-ads"))
      val bodyText: String = contentAsString(afterDeleteResult)
      val json = Json.parse(bodyText)
      val titles = (json \ "adverts" \\ "title")
        .map(_.as[String]) //to convert JsString to String

      status(result) must be (202)
      titles must be (List("Audi A4", "Kia Ceed"))
    }

    "respond with `Not Modified` " +
      "if there is no car with specified id to delete" in {
      val controller = new CarAdvertController()
      val result: Future[Result] = controller.delete(111)
        .apply(FakeRequest(GET, "/api/car-ad/111"))

      val afterDeleteResult = controller.index()
        .apply(FakeRequest(GET, "/api/car-ads"))
      val bodyText: String = contentAsString(afterDeleteResult)
      val json = Json.parse(bodyText)
      val titles = (json \ "adverts" \\ "title")
        .map(_.as[String]) //to convert JsString to String

      status(result) must be (304)
      titles must be (List("Audi A4", "Kia Ceed", "Skoda Octavia"))
    }
  }
}
