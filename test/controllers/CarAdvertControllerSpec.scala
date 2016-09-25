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

      status(result) must be(204)
    }
  }

  "Car Advert #delete(id)" must {
    "delete car advert by id" in {
      val controller = new CarAdvertController()
      val result: Future[Result] = controller.delete(777)
        .apply(FakeRequest(DELETE, "/api/car-ad/777"))

      val afterDeleteResult: Future[Result] = controller.index()
        .apply(FakeRequest(GET, "/api/car-ads"))
      val bodyText: String = contentAsString(afterDeleteResult)
      val json = Json.parse(bodyText)
      val titles = (json \ "adverts" \\ "title")
        .map(_.as[String]) //to convert JsString to String

      status(result) must be(202)
      titles must be(List("Audi A4", "Kia Ceed"))
    }

    "respond with `Not Modified` " +
      "if there is no car with specified id to delete" in {
      val controller = new CarAdvertController()
      val result: Future[Result] = controller.delete(111)
        .apply(FakeRequest(DELETE, "/api/car-ad/111"))

      val afterDeleteResult = controller.index()
        .apply(FakeRequest(GET, "/api/car-ads"))
      val bodyText: String = contentAsString(afterDeleteResult)
      val json = Json.parse(bodyText)
      val titles = (json \ "adverts" \\ "title")
        .map(_.as[String]) //to convert JsString to String

      status(result) must be(304)
      titles must be(List("Audi A4", "Kia Ceed", "Skoda Octavia"))
    }
  }

  "Car Advert#create" must {
    "add new car" in {

      val jsonReq = Json.obj(
        "title" -> "Lanos",
        "fuel" -> "gasoline",
        "price" -> 10000,
        "new" -> true
      )
      val controller = new CarAdvertController()
      val request = FakeRequest(PUT, "/api/car-ad")
        .withJsonBody(jsonReq)

      val result: Future[Result] = controller.create()(request)

      val afterCreateResult = controller.index()
        .apply(FakeRequest(GET, "/api/car-ads"))
      val bodyText: String = contentAsString(afterCreateResult)
      val json = Json.parse(bodyText)
      val titles = (json \ "adverts" \\ "title")
        .map(_.as[String]) //to convert JsString to String

      status(result) must be(201)
      titles must be(List("Audi A4", "Kia Ceed", "Skoda Octavia", "Lanos"))
    }

    "add old car" in {

      val jsonReq = Json.obj(
        "title" -> "Lanos",
        "fuel" -> "gasoline",
        "price" -> 10000,
        "new" -> false,
        "mileage" -> 30000,
        "first_registration" -> "2002-04-07"
      )
      val controller = new CarAdvertController()
      val request = FakeRequest(PUT, "/api/car-ad")
        .withJsonBody(jsonReq)

      val result: Future[Result] = controller.create()(request)

      val afterCreateResult = controller.index()
        .apply(FakeRequest(GET, "/api/car-ads"))
      val bodyText: String = contentAsString(afterCreateResult)
      val json = Json.parse(bodyText)
      val titles = (json \ "adverts" \\ "title")
        .map(_.as[String]) //to convert JsString to String

      status(result) must be(201)
      titles must be(List("Audi A4", "Kia Ceed", "Skoda Octavia", "Lanos"))
    }

   "not add car advert if there is no mileage for old car" in {
      val jsonReq = Json.obj(
        "title" -> "Lanos",
        "fuel" -> "gasoline",
        "price" -> 10000,
        "new" -> false
      )

     val controller = new CarAdvertController()
     val request = FakeRequest(PUT, "/api/car-ad")
        .withJsonBody(jsonReq)

      val result: Future[Result] = controller.create()(request)

      val afterCreateResult = controller.index()
        .apply(FakeRequest(GET, "/api/car-ads"))
      val bodyText: String = contentAsString(afterCreateResult)
      val json = Json.parse(bodyText)
      val titles = (json \ "adverts" \\ "title")
        .map(_.as[String]) //to convert JsString to String

      status(result) must be(400)
      assert(contentAsString(result) contains ("`new` and `first_registration` are mandatory for old cars."))
      titles must be(List("Audi A4", "Kia Ceed", "Skoda Octavia"))
    }

    "not add car advert if there is no first_registration for old car" in {
      val jsonReq = Json.obj(
        "title" -> "Lanos",
        "fuel" -> "gasoline",
        "price" -> 10000,
        "new" -> false
      )
      val controller = new CarAdvertController()
      val request = FakeRequest(PUT, "/api/car-ad")
        .withJsonBody(jsonReq)

      val result: Future[Result] = controller.create()(request)

      val afterCreateResult = controller.index()
        .apply(FakeRequest(GET, "/api/car-ads"))
      val bodyText: String = contentAsString(afterCreateResult)
      val json = Json.parse(bodyText)
      val titles = (json \ "adverts" \\ "title")
        .map(_.as[String]) //to convert JsString to String

      status(result) must be(400)
      assert(contentAsString(result) contains ("`new` and `first_registration` are mandatory for old cars."))
      titles must be(List("Audi A4", "Kia Ceed", "Skoda Octavia"))
    }
  }

  "Car Advert#update(id)" must{
    "modify car advert by id" in {
      val jsonReq = Json.obj(
        "title" -> "Lanos",
        "fuel" -> "gasoline",
        "price" -> 10000,
        "new" -> false,
        "mileage" -> 30000,
        "first_registration" -> "2002-04-07"
      )
      val controller = new CarAdvertController()
      val request = FakeRequest(POST, "/api/car-ad/777")
        .withJsonBody(jsonReq)
      val result: Future[Result] = controller.update(777)(request)

      val afterCreateResult = controller.index()
        .apply(FakeRequest(GET, "/api/car-ads"))
      val bodyText: String = contentAsString(afterCreateResult)
      val json = Json.parse(bodyText)
      val titles = (json \ "adverts" \\ "title")
        .map(_.as[String]) //to convert JsString to String

      status(result) must be(202)
      titles must be(List("Audi A4", "Kia Ceed", "Lanos"))
    }

    "not modify car advert by id if there is no such id" in {
      val jsonReq = Json.obj(
        "title" -> "Lanos",
        "fuel" -> "gasoline",
        "price" -> 10000,
        "new" -> false,
        "mileage" -> 30000,
        "first_registration" -> "2002-04-07"
      )

      val controller = new CarAdvertController()
      val request = FakeRequest(POST, "/api/car-ad/111")
        .withJsonBody(jsonReq)
      val result: Future[Result] = controller.update(111)(request)

      val afterCreateResult = controller.index()
        .apply(FakeRequest(GET, "/api/car-ads"))
      val bodyText: String = contentAsString(afterCreateResult)
      val json = Json.parse(bodyText)
      val titles = (json \ "adverts" \\ "title")
        .map(_.as[String]) //to convert JsString to String

      status(result) must be(304)
      titles must be(List("Audi A4", "Kia Ceed", "Skoda Octavia"))
    }

    "not modify old car advert by id if there is no mileage" in{
      val jsonReq = Json.obj(
        "title" -> "Lanos",
        "fuel" -> "gasoline",
        "price" -> 10000,
        "new" -> false,
        "first_registration" -> "2002-04-07"
      )

      val controller = new CarAdvertController()
      val request = FakeRequest(POST, "/api/car-ad/111")
        .withJsonBody(jsonReq)
      val result: Future[Result] = controller.update(111)(request)

      val afterCreateResult = controller.index()
        .apply(FakeRequest(GET, "/api/car-ads"))
      val bodyText: String = contentAsString(afterCreateResult)
      val json = Json.parse(bodyText)
      val titles = (json \ "adverts" \\ "title")
        .map(_.as[String]) //to convert JsString to String

      status(result) must be(400)
      titles must be(List("Audi A4", "Kia Ceed", "Skoda Octavia"))
    }

    "not modify old car advert by id if there is no first_registration" in{
      val jsonReq = Json.obj(
        "title" -> "Lanos",
        "fuel" -> "gasoline",
        "price" -> 10000,
        "new" -> false,
        "mileage" -> 30000
      )

      val controller = new CarAdvertController()
      val request = FakeRequest(POST, "/api/car-ad/111")
        .withJsonBody(jsonReq)
      val result: Future[Result] = controller.update(111)(request)

      val afterCreateResult = controller.index()
        .apply(FakeRequest(GET, "/api/car-ads"))
      val bodyText: String = contentAsString(afterCreateResult)
      val json = Json.parse(bodyText)
      val titles = (json \ "adverts" \\ "title")
        .map(_.as[String]) //to convert JsString to String

      status(result) must be(400)
      titles must be(List("Audi A4", "Kia Ceed", "Skoda Octavia"))
    }
  }
}
