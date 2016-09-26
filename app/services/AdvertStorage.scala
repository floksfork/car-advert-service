package services

import javax.inject.Inject

import com.google.inject.Singleton
import controllers.Car

trait AdvertStorage {

  //TODO: switch to DB storage lately.
  var data: List[Car] = List()

  def find(): List[Car]

  def create(c: Car): Unit

  def read(id: Int): Option[Car]

  def update(c: Car): Boolean

  def delete(id: Int): Boolean
}

@Singleton
class AdvertStorageImpl @Inject() extends AdvertStorage {
  override def find(): List[Car] = data

  override def update(car: Car): Boolean = data.find(c => c.id == Option(car.id)) match {
    case Some(carForUpdate) =>
      data = data.filter(c => c.id != Option(car.id)) ::: List(
        carForUpdate.copy(
          title = car.title, fuel = car.fuel,
          price = car.price, isNew = car.isNew,
          mileage = car.mileage, firstReg = car.firstReg
        )
      )
      true

    case None => false
  }


  override def delete(id: Int): Boolean = data.find(c => c.id == Option(id)) match {
    case Some(car) =>
      data = data.filter(c => c.id != Option(id))
      true

    case None => false
  }

  override def read(id: Int): Option[Car] = data.find(ad => ad.id == Option(id))

  override def create(c: Car) = {
    data = data ::: List(c)
  }
}
