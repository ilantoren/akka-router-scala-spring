package sample

import akka.actor.{ActorLogging, Actor}

/**
 * Created by Richard Thorne (ilan toren) on 6/18/14.
 *  An actor that uses the SlickService
 */

class SlickActor extends Actor with ActorLogging {

  var slickService: SlickService = _

  def receive = {
    case x : FibonacciDAO => {
      slickService.save(x.num,x.fibonacci,x.processtime)
      sender ! "saved in db the fibonacci for " + x.num
    }
    case _  => log.info("bad message")
  }


}
