package controllers

import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play._
import play.api.libs.json.Json
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test._
import services.{AdvertStorage, IDGenerator}

import scala.concurrent.Future

class CarAdvertControllerSpec extends PlaySpec with Results with MockitoSugar {
  val generator = mock[IDGenerator]

  val car1 = Car(Some(22), "Audi A4", Fuel("diesel"), 10000, false, Option(43000), Option(DateUtil.strToDate("2008-09-14").get))
  val car2 = Car(Some(33), "Kia Ceed", Fuel("gasoline"), 8000, false, Option(27000), Option(DateUtil.strToDate("2013-05-28").get))
  val car3 = Car(Some(777), "Skoda Octavia", Fuel("diesel"), 25000, true, None, None)
  val car4 = Car(Some(999), "Lanos", Fuel("gasoline"), 10000, true, None, None)


  "Car Advert #index" must {
    "return list of all car adverts ordered by id" in {
      val storage = mock[AdvertStorage]
      when(storage.find()) thenReturn List(car1, car2, car3)
      val controller = new CarAdvertController(generator, storage)
      val result: Future[Result] = controller.show("id")
        .apply(FakeRequest(GET, "/api/car-ads"))
      val bodyText: String = contentAsString(result)
      val json = Json.parse(bodyText)

      val titles = (json \ "adverts" \\ "title")
        .map(_.as[String]) //to convert JsString to String

      val isNews = (json \ "adverts" \\ "new")
        .map(_.as[Boolean])

      titles mustBe List("Audi A4", "Kia Ceed", "Skoda Octavia")
      isNews mustBe List(false, false, true)
    }

    "return list of all car adverts ordered by title" in {
      val storage = mock[AdvertStorage]
      when(storage.find()) thenReturn List(car1, car2, car3)
      val controller = new CarAdvertController(generator, storage)
      val result: Future[Result] = controller.show("title")
        .apply(FakeRequest(GET, "/api/car-ads"))
      val bodyText: String = contentAsString(result)
      val json = Json.parse(bodyText)

      val titles = (json \ "adverts" \\ "title")
        .map(_.as[String]) //to convert JsString to String

      titles mustBe List("Audi A4", "Kia Ceed", "Skoda Octavia")
    }

    "return list of all car adverts ordered by fuel" in {
      val storage = mock[AdvertStorage]
      when(storage.find()) thenReturn List(car1, car2, car3)
      val controller = new CarAdvertController(generator, storage)
      val result: Future[Result] = controller.show("fuel")
        .apply(FakeRequest(GET, "/api/car-ads"))
      val bodyText: String = contentAsString(result)
      val json = Json.parse(bodyText)

      val titles = (json \ "adverts" \\ "title")
        .map(_.as[String]) //to convert JsString to String

      titles mustBe List("Audi A4", "Skoda Octavia", "Kia Ceed")
    }

    "return list of all car adverts ordered by price" in {
      val storage = mock[AdvertStorage]
      when(storage.find()) thenReturn List(car1, car2, car3)
      val controller = new CarAdvertController(generator, storage)
      val result: Future[Result] = controller.show("price")
        .apply(FakeRequest(GET, "/api/car-ads"))
      val bodyText: String = contentAsString(result)
      val json = Json.parse(bodyText)

      val titles = (json \ "adverts" \\ "title")
        .map(_.as[String]) //to convert JsString to String

      titles mustBe List("Kia Ceed", "Audi A4", "Skoda Octavia")
    }

    "return list of all car adverts ordered by new" in {
      val storage = mock[AdvertStorage]
      when(storage.find()) thenReturn List(car1, car2, car3)
      val controller = new CarAdvertController(generator, storage)
      val result: Future[Result] = controller.show("new")
        .apply(FakeRequest(GET, "/api/car-ads"))
      val bodyText: String = contentAsString(result)
      val json = Json.parse(bodyText)

      val titles = (json \ "adverts" \\ "title")
        .map(_.as[String]) //to convert JsString to String

      titles mustBe List("Skoda Octavia", "Audi A4", "Kia Ceed")
    }

    "return list of all car adverts ordered by mileage" in {
      val storage = mock[AdvertStorage]
      when(storage.find()) thenReturn List(car1, car2, car3)
      val controller = new CarAdvertController(generator, storage)
      val result: Future[Result] = controller.show("mileage")
        .apply(FakeRequest(GET, "/api/car-ads"))
      val bodyText: String = contentAsString(result)
      val json = Json.parse(bodyText)

      val titles = (json \ "adverts" \\ "title")
        .map(_.as[String]) //to convert JsString to String

      titles mustBe List("Skoda Octavia", "Kia Ceed", "Audi A4")
    }

    "return list of all car adverts ordered by first_registration" in {
      val storage = mock[AdvertStorage]
      when(storage.find()) thenReturn List(car1, car2, car3)
      val controller = new CarAdvertController(generator, storage)
      val result: Future[Result] = controller.show("first_registration")
        .apply(FakeRequest(GET, "/api/car-ads"))
      val bodyText: String = contentAsString(result)
      val json = Json.parse(bodyText)

      val titles = (json \ "adverts" \\ "title")
        .map(_.as[String]) //to convert JsString to String

      titles mustBe List("Audi A4", "Kia Ceed", "Skoda Octavia")
    }
  }

  "Car Advert #read(id)" must {
    "return data for single car advert by id" in {
      val storage = mock[AdvertStorage]
      when(storage.read(any[Int])) thenReturn Option(car3)
      val controller = new CarAdvertController(generator, storage)
      val result: Future[Result] = controller.read(777)
        .apply(FakeRequest(GET, "/api/car-ad/777"))
      val bodyText: String = contentAsString(result)
      val json = Json.parse(bodyText)

      (json \ "id").as[Int] must be(777)
      (json \ "title").as[String] must be("Skoda Octavia")
      (json \ "fuel").as[String] must be("diesel")
      (json \ "new").as[Boolean] must be(true)
      (json \ "mileage").as[String] must be("")
      (json \ "first_registration").as[String] must be("")
    }

    "return `No Content` for single car advert if no data was" +
      "found for the specified id" in {
      val storage = mock[AdvertStorage]
      when(storage.read(any[Int])) thenReturn None

      val controller = new CarAdvertController(generator, storage)
      val result: Future[Result] = controller.read(111)
        .apply(FakeRequest(GET, "/api/car-ad/111"))

      status(result) must be(NO_CONTENT)
    }
  }

  "Car Advert #delete(id)" must {
    "delete car advert by id" in {
      val storage = mock[AdvertStorage]
      when(storage.find()) thenReturn List(car1, car2)
      when(storage.delete(777)) thenReturn true

      val controller = new CarAdvertController(generator, storage)
      val result: Future[Result] = controller.delete(777)
        .apply(FakeRequest(DELETE, "/api/car-ad/777"))

      val afterDeleteResult: Future[Result] = controller.show("id")
        .apply(FakeRequest(GET, "/api/car-ads"))
      val bodyText: String = contentAsString(afterDeleteResult)
      val json = Json.parse(bodyText)
      val titles = (json \ "adverts" \\ "title")
        .map(_.as[String]) //to convert JsString to String

      status(result) must be(ACCEPTED)
      titles must be(List("Audi A4", "Kia Ceed"))
    }

    "respond with `Not Modified` " +
      "if there is no car with specified id to delete" in {
      val storage = mock[AdvertStorage]
      when(storage.find()) thenReturn List(car1, car2, car3)
      when(storage.delete(111)) thenReturn false

      val controller = new CarAdvertController(generator, storage)
      val result: Future[Result] = controller.delete(111)
        .apply(FakeRequest(DELETE, "/api/car-ad/111"))

      val afterDeleteResult = controller.show("id")
        .apply(FakeRequest(GET, "/api/car-ads"))
      val bodyText: String = contentAsString(afterDeleteResult)
      val json = Json.parse(bodyText)
      val titles = (json \ "adverts" \\ "title")
        .map(_.as[String]) //to convert JsString to String

      status(result) must be(NOT_MODIFIED)
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
      val storage = mock[AdvertStorage]
      when(storage.find()) thenReturn List(car1, car2, car3, car4)
      val controller = new CarAdvertController(generator, storage)
      val request = FakeRequest(PUT, "/api/car-ad")
        .withJsonBody(jsonReq)

      val result: Future[Result] = controller.create()(request)

      val afterCreateResult = controller.show("id")
        .apply(FakeRequest(GET, "/api/car-ads"))
      val bodyText: String = contentAsString(afterCreateResult)
      val json = Json.parse(bodyText)
      val titles = (json \ "adverts" \\ "title")
        .map(_.as[String]) //to convert JsString to String

      status(result) must be(CREATED)
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
      val oldCar = Car(Some(999), "Lanos", Fuel("gasoline"), 10000, false, Option(30000), Option(DateUtil.strToDate("2002-04-07").get))
      val storage = mock[AdvertStorage]
      when(storage.find()) thenReturn List(car1, car2, car3, oldCar)
      val controller = new CarAdvertController(generator, storage)
      val request = FakeRequest(PUT, "/api/car-ad")
        .withJsonBody(jsonReq)

      val result: Future[Result] = controller.create()(request)

      val afterCreateResult = controller.show("id")
        .apply(FakeRequest(GET, "/api/car-ads"))
      val bodyText: String = contentAsString(afterCreateResult)
      val json = Json.parse(bodyText)
      val titles = (json \ "adverts" \\ "title")
        .map(_.as[String]) //to convert JsString to String

      status(result) must be(CREATED)
      titles must be(List("Audi A4", "Kia Ceed", "Skoda Octavia", "Lanos"))
    }

    "not add car advert if there is no mileage for old car" in {
      val jsonReq = Json.obj(
        "title" -> "Lanos",
        "fuel" -> "gasoline",
        "price" -> 10000,
        "new" -> false
      )

      val storage = mock[AdvertStorage]
      when(storage.find()) thenReturn List(car1, car2, car3)
      val controller = new CarAdvertController(generator, storage)
      val request = FakeRequest(PUT, "/api/car-ad")
        .withJsonBody(jsonReq)

      val result: Future[Result] = controller.create()(request)

      val afterCreateResult = controller.show("id")
        .apply(FakeRequest(GET, "/api/car-ads"))
      val bodyText: String = contentAsString(afterCreateResult)
      val json = Json.parse(bodyText)
      val titles = (json \ "adverts" \\ "title")
        .map(_.as[String]) //to convert JsString to String

      status(result) must be(BAD_REQUEST)
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
      val storage = mock[AdvertStorage]
      when(storage.find()) thenReturn List(car1, car2, car3)
      val controller = new CarAdvertController(generator, storage)
      val request = FakeRequest(PUT, "/api/car-ad")
        .withJsonBody(jsonReq)

      val result: Future[Result] = controller.create()(request)

      val afterCreateResult = controller.show("id")
        .apply(FakeRequest(GET, "/api/car-ads"))
      val bodyText: String = contentAsString(afterCreateResult)
      val json = Json.parse(bodyText)
      val titles = (json \ "adverts" \\ "title")
        .map(_.as[String]) //to convert JsString to String

      status(result) must be(BAD_REQUEST)
      assert(contentAsString(result) contains ("`new` and `first_registration` are mandatory for old cars."))
      titles must be(List("Audi A4", "Kia Ceed", "Skoda Octavia"))
    }
  }

  "Car Advert#update(id)" must {
    "modify car advert by id" in {
      val jsonReq = Json.obj(
        "title" -> "Lanos",
        "fuel" -> "gasoline",
        "price" -> 10000,
        "new" -> false,
        "mileage" -> 30000,
        "first_registration" -> "2002-04-07"
      )
      val car = Car(Some(777), "Lanos", Fuel("gasoline"), 10000, false, Option(30000), Option(DateUtil.strToDate("2002-04-07").get))
      val storage = mock[AdvertStorage]
      when(storage.update(any[Car])) thenReturn true
      when(storage.find()) thenReturn List(car1, car2, car)
      val controller = new CarAdvertController(generator, storage)
      val request = FakeRequest(POST, "/api/car-ad/777")
        .withJsonBody(jsonReq)
      val result: Future[Result] = controller.update(777)(request)

      val afterCreateResult = controller.show("id")
        .apply(FakeRequest(GET, "/api/car-ads"))
      val bodyText: String = contentAsString(afterCreateResult)
      val json = Json.parse(bodyText)
      val titles = (json \ "adverts" \\ "title")
        .map(_.as[String]) //to convert JsString to String

      status(result) must be(ACCEPTED)
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

      val storage = mock[AdvertStorage]
      when(storage.find()) thenReturn List(car1, car2, car3)
      val controller = new CarAdvertController(generator, storage)
      val request = FakeRequest(POST, "/api/car-ad/111")
        .withJsonBody(jsonReq)
      val result: Future[Result] = controller.update(111)(request)

      val afterCreateResult = controller.show("id")
        .apply(FakeRequest(GET, "/api/car-ads"))
      val bodyText: String = contentAsString(afterCreateResult)
      val json = Json.parse(bodyText)
      val titles = (json \ "adverts" \\ "title")
        .map(_.as[String]) //to convert JsString to String

      status(result) must be(NOT_MODIFIED)
      titles must be(List("Audi A4", "Kia Ceed", "Skoda Octavia"))
    }

    "not modify old car advert by id if there is no mileage" in {
      val jsonReq = Json.obj(
        "title" -> "Lanos",
        "fuel" -> "gasoline",
        "price" -> 10000,
        "new" -> false,
        "first_registration" -> "2002-04-07"
      )

      val storage = mock[AdvertStorage]
      when(storage.find()) thenReturn List(car1, car2, car3)
      val controller = new CarAdvertController(generator, storage)
      val request = FakeRequest(POST, "/api/car-ad/111")
        .withJsonBody(jsonReq)
      val result: Future[Result] = controller.update(111)(request)

      val afterCreateResult = controller.show("id")
        .apply(FakeRequest(GET, "/api/car-ads"))
      val bodyText: String = contentAsString(afterCreateResult)
      val json = Json.parse(bodyText)
      val titles = (json \ "adverts" \\ "title")
        .map(_.as[String]) //to convert JsString to String

      status(result) must be(BAD_REQUEST)
      titles must be(List("Audi A4", "Kia Ceed", "Skoda Octavia"))
    }

    "not modify old car advert by id if there is no first_registration" in {
      val jsonReq = Json.obj(
        "title" -> "Lanos",
        "fuel" -> "gasoline",
        "price" -> 10000,
        "new" -> false,
        "mileage" -> 30000
      )

      val storage = mock[AdvertStorage]
      when(storage.find()) thenReturn List(car1, car2, car3)
      val controller = new CarAdvertController(generator, storage)
      val request = FakeRequest(POST, "/api/car-ad/111")
        .withJsonBody(jsonReq)
      val result: Future[Result] = controller.update(111)(request)

      val afterCreateResult = controller.show("id")
        .apply(FakeRequest(GET, "/api/car-ads"))
      val bodyText: String = contentAsString(afterCreateResult)
      val json = Json.parse(bodyText)
      val titles = (json \ "adverts" \\ "title")
        .map(_.as[String]) //to convert JsString to String

      status(result) must be(BAD_REQUEST)
      titles must be(List("Audi A4", "Kia Ceed", "Skoda Octavia"))
    }
  }
}
