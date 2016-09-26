package controllers

import javax.inject._

import play.api.mvc._
import services.{AdvertStorage, IDGenerator}

@Singleton
class HomeController @Inject()(idGenerator: IDGenerator, storage: AdvertStorage) extends Controller {
  def index = Action {
    storage.create(
      Car(Some(idGenerator.generate()), "Audi A4", Fuel("diesel"), 10000, false, Option(43000), Option(DateUtil.strToDate("2008-09-14").get))
    )
    storage.create(
      Car(Some(idGenerator.generate()), "Kia Ceed", Fuel("gasoline"), 8000, false, Option(27000), Option(DateUtil.strToDate("2013-05-28").get))
    )
    storage.create(
      Car(Some(idGenerator.generate()), "Skoda Octavia", Fuel("diesel"), 25000, true, None, None)
    )
    PermanentRedirect("/api/car-ads")
  }

}
