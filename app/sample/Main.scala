package sample

import akka.actor._
import org.springframework.scala.context.function._
import akka.routing.{Broadcast, RoundRobinRouter}
import akka.pattern.ask
import scala.util._
import sample.FibonacciCollector.GET


object Main extends App {

  import Config._


  val sample = List( 1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,20,25,30,35,40,45,50,60,70,80,90,100,200,300,600)
  var  sent = false
  val system = ActorSystem("fibonacci")
  val collector = system.actorOf(Props(new FibonacciCollector(sample)), "collector")
  sent = true
  var loopTest = 0
  while (sent  && loopTest < 1) {
    val result = (collector ? GET ).mapTo[Int]

    result onComplete {
      case Success(result) => {
          loopTest = result
        }

      case Failure(failure) =>{
        println(s"Got an exception $failure")
        loopTest = 1
      }
    }
    Thread.sleep(2000)
  }

  system.shutdown
  system.awaitTermination
  System.exit(1)
 }



object FibonacciCollector {
  object GET
}
class FibonacciCollector(sample: List[Int] ) extends Actor with ActorLogging {

  import FibonacciCollector._
  var list: List[BigInt] = Nil
  var size = sample.size
  var isDead = 0;
  println( s"size is $size")
  // create a spring context
  implicit val ctx = FunctionalConfigApplicationContext(classOf[AppConfiguration])


  // get hold of the actor system
  val system = ctx.getBean(classOf[ActorSystem])

  val prop = SpringExtentionImpl(system).props("fibonacciActor")

  // use the Spring Extension to create props for a named actor bean
  val actor1 = system.actorOf(prop)
  val actor2 = system.actorOf(prop)
  val actor3 = system.actorOf(prop)
  val actor4 = system.actorOf(prop)
  val routees = Vector[ActorRef](actor1, actor2, actor3, actor4)
  val router = system.actorOf(prop.withRouter(RoundRobinRouter(routees = routees)))

  //val fibonacciActor = system.actorOf(prop, "fibonacciActor")
  for (num <- sample) {
    log.info(s"starting actor with $num")
    router ! num
  }

  context.watch(router)
  router ! Broadcast( PoisonPill )
  def receive = {
    case (num: Int, fib: BigInt) => {
      log.info(s"fibonacci for $num is $fib")
      //log.info(s"fibonacci for $num")
      list = num :: list
      size -= 1

      //log.info(s"size is $size")
      if (size == 0) {
        log.info("should shutdown")
      }
    }
    case Terminated(corpse) => {
      log.info( "all routees are dead")
      isDead = 1
      context.system.shutdown()
    }

    case GET =>{
          log.info("Sending isDead status")
          sender ! isDead
    }
    case _  => {
      log.info("no match")
    }
  }
}