package services

import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Singleton

trait IDGenerator {
  def generate(): Int
}

@Singleton
class AtomicIDGenerator extends IDGenerator {
  private val atomicCounter = new AtomicInteger(18)
  override def generate(): Int = atomicCounter.getAndIncrement()
}
