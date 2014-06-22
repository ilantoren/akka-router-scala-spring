package sample

import akka.actor._
import org.springframework.scala.context.function._
import akka.routing.{SmallestMailboxRouter, Broadcast}
import akka.pattern.ask
import scala.util._
import sample.FibonacciCollector.GET
import scala.concurrent.Future
import scala.annotation.tailrec


object Main extends App {

  import Config._


  val sample = List(10000, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 20, 25, 30, 35, 40, 45, 50, 60, 70, 80, 90, 100, 200, 300, 600)
  val system = ActorSystem("fibonacci")
  val collector = system.actorOf(Props(new FibonacciCollector(sample)), "collector")

  isAlive

  system.shutdown
  system.awaitTermination

  System.exit(1)

  @tailrec
  def isAlive : Int = {
    val status = (collector ? GET).mapTo[Int]
    var res = checkAlive( status )
    res match {
      case 1 =>  isAlive
      case 0 =>   0
    }
  }

  def checkAlive(status : Future[Int]): Int = {
    var res = 0
    status onComplete {
      case Success(status) => res = status

      case Failure(failure) => {
        println(s"Got an exception $failure")
        res = 1
      }
    }
    Thread.sleep(2000)
    res
  }
}


object FibonacciCollector {
  object GET
}

class FibonacciCollector(sample: List[Int]) extends Actor with ActorLogging {

  import FibonacciCollector._

  var list: List[BigInt] = Nil
  var size = sample.size
  var isDead = 0;
  println(s"size is $size")
  // create a spring context
  implicit val ctx = FunctionalConfigApplicationContext(classOf[AppConfiguration])


  // get hold of the actor system
  val system = ctx.getBean(classOf[ActorSystem])
  // retrieve the properties
  val prop = SpringExtentionImpl(system).props("fibonacciActor")

  // use the Spring Extension to create props for a named actor bean
  val actor1 = system.actorOf(prop)
  val actor2 = system.actorOf(prop)
  val actor3 = system.actorOf(prop)
  val actor4 = system.actorOf(prop)
  val routees = Vector[ActorRef](actor1, actor2, actor3, actor4)
  val router = system.actorOf(prop.withRouter(SmallestMailboxRouter(routees = routees)))

  for (num <- sample) {
    log.info(s"starting actor with $num")
    router ! num
  }
  // set up deathwatch reaper
  context.watch(router)

  // Broadcast poison pill via the router to all client actors to terminate at last message
  router ! Broadcast(PoisonPill)

  def receive = {
    case (num: Int, fib: BigInt) => {
      log.info(s"fibonacci for $num is $fib")
      list = num :: list
      size -= 1

      // informational not required
      if (size == 0) {
        log.info("should shutdown")
      }
    }
    // router terminates when it has no active actor clients
    // reaper sends message with router shuts down
    case Terminated(corpse) => {
      log.info("all routees are dead")
      isDead = 1
      context.system.shutdown()
    }

    // main  asks the collector if it is still active
    case GET => {
      log.info("Sending isDead status")
      sender ! isDead
    }
    // shouldn't get here, but ....
    case _ => {
      log.info("no match")
    }
  }
}