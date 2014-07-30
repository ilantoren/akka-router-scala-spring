package sample

import akka.actor.SupervisorStrategy._
import akka.actor.{Actor, ActorLogging, OneForOneStrategy}

import scala.concurrent.duration._

/**
 * Created by Richard Thorne (ilan toren) on 6/18/14.
 * An actor that uses the FibonacciService
 */

class FibonacciActor extends Actor with ActorLogging {
  var startTime = new java.util.Date().getTime()
  var fibonacciService: FibonacciService = _

  def receive = {
    case n: Int => {
      sender !(n, fibonacciService.fibonacci(n), startTime)
    }
  }

  // Restart the storage child when StorageException is thrown.
  // After 3 restarts within 5 seconds it will be stopped.
  override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = 3,
    withinTimeRange = 5 seconds) {
    case _ => Restart
  }
}
