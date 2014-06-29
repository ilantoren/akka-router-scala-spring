package sample

import akka.actor.{ActorLogging, Actor}

/**
 * Created by Richard Thorne (ilan toren) on 6/18/14.
 *  An actor that uses the FibonacciService
 */

class FibonacciActor extends Actor with ActorLogging {
  var startTime = new java.util.Date().getTime()
  var fibonacciService: FibonacciService = _

  def receive = {
    case n:Int => {
      sender ! (n, fibonacciService.fibonacci(n), startTime)
    }
  }


}
