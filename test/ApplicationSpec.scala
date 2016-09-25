import org.scalatestplus.play._
import play.api.libs.json.Json
import play.api.test._
import play.api.test.Helpers._

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
class ApplicationSpec extends PlaySpec with OneAppPerTest {

  "Routes" should {

    "send 404 on a bad request" in  {
      route(app, FakeRequest(GET, "/boum")).map(status(_)) mustBe Some(NOT_FOUND)
    }

  }

  "HomeController" should {

    "render the index page" in {
      val home = route(app, FakeRequest(GET, "/")).get

      status(home) mustBe PERMANENT_REDIRECT
      contentType(home) mustBe None
    }

  }

  "CarAdvertController" should {
    "return list of all car adverts" in {
      val resp = route(app, FakeRequest(GET, "/api/car-ads")).get

      status(resp) mustBe OK
      contentType(resp) mustBe Some("application/json")
    }

    "return a car advert for id" in {
      val resp = route(app, FakeRequest(GET, "/api/car-ad/18")).get

      status(resp) mustBe OK
      contentType(resp) mustBe Some("application/json")
    }

    "create a car advert" in {
      val jsonReq = Json.obj(
        "title" -> "Lanos",
        "fuel" -> "gasoline",
        "price" -> 10000,
        "new" -> true
      )
      val req = FakeRequest(PUT, "/api/car-ad").withJsonBody(jsonReq)
      val resp = route(app, req).get

      status(resp) mustBe CREATED
      contentType(resp) mustBe Some("application/json")
    }

    "update a car advert" in {
      val jsonReq = Json.obj(
        "title" -> "Lanos",
        "fuel" -> "gasoline",
        "price" -> 10000,
        "new" -> false,
        "mileage" -> 30000,
        "first_registration" -> "2002-04-07"
      )
      val req = FakeRequest(POST, "/api/car-ad/19").withJsonBody(jsonReq)
      val resp = route(app, req).get

      status(resp) mustBe ACCEPTED
      contentType(resp) mustBe Some("application/json")
    }

    "delete a car advert" in {
      val req = FakeRequest(DELETE, "/api/car-ad/19")
      val resp = route(app, req).get

      status(resp) mustBe ACCEPTED
      contentType(resp) mustBe Some("application/json")
    }

    "view options for the web service" in {
      val req = FakeRequest(OPTIONS, "/api/car-ads")
      val resp = route(app, req).get

      status(resp) mustBe OK
      contentType(resp) mustBe Some("application/json")
    }
  }

}
