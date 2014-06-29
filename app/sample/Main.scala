package sample

import akka.actor._
import org.springframework.scala.context.function._
import akka.routing.{Broadcast, RoundRobinRouter}
import akka.pattern.ask
import scala.util._
import sample.FibonacciCollector.GET
import scala.annotation.tailrec


object Main extends App {

  import Config._


  val sample = List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 20, 25, 30, 35, 40, 45, 50, 60, 70, 80, 90, 100, 200, 300, 600)
  val system = ActorSystem("fibonacci")
  val collector = system.actorOf(Props(new FibonacciCollector(sample)), "collector")

  // the SlickService is DI by Spring see AppConfiguration
  var slickService: SlickService = _

  // tail recursive check instead of a while loop
  loopCheck(0)

  println("PRINT RESULTS")
  val saved: List[FibonacciDAO] = slickService.findAll()
  saved.foreach(
    x => println(s"From the DB:  when num is ${x.num} the fibonacci number is ${x.fibonacci} time: ${x.processtime} mS")
  )

  system.shutdown
  system.awaitTermination


  @tailrec
  def loopCheck(loopTest: Int): Int = loopTest match {
    case 0 => {
      val test: Int = askCollector
      loopCheck(test)
    }
    case 1 => println("All done")

    1
  }

  def askCollector: Int = {
    Thread.sleep(2000)
    val result = (collector ? GET).mapTo[Int]
    var ret = 0
    result onComplete {
      case Success(result) => {
        ret = result
      }
      case Failure(failure) => {
        println(s"Got an exception $failure")
        ret = 1
      }
    }
    ret
  }
}




object FibonacciCollector {
  object GET
}
class FibonacciCollector(sample: List[Int] ) extends Actor with ActorLogging {

  import FibonacciCollector._
  var list: List[BigInt] = Nil
  var size = sample.size
  var isDead = 0;
  println( s"size of list is $size")
  // create a spring context
  implicit val ctx = FunctionalConfigApplicationContext(classOf[AppConfiguration])


  // get hold of the actor system
  val system = ctx.getBean(classOf[ActorSystem])

  // use the Spring Extension to create props for a named actor bean
  val prop = SpringExtentionImpl(system).props("fibonacciActor")
  val prop2 = SpringExtentionImpl(system).props("slickActor")

  // One way to set routees is to add then to the router in a vector use-case when there is a value needed
  //    in the constructor
  val actor1 = system.actorOf(prop)
  val actor2 = system.actorOf(prop)
  val actor3 = system.actorOf(prop)
  val actor4 = system.actorOf(prop)
  val routees = Vector[ActorRef](actor1, actor2, actor3, actor4)
  val router = system.actorOf(prop.withRouter(RoundRobinRouter(routees = routees)))

  // Or more simply
  val router2 = system.actorOf(prop2.withRouter(RoundRobinRouter(nrOfInstances = 5)) )

  for (num <- sample) {
    log.info(s"starting Fibonacci actor with $num")
    router ! num
  }

  //  Death watch call back on routers
  context.watch(router)
  context.watch(router2)

  //  first level has no more messages - send message via router for each child will shut down
  //   the router then dies when it has no live children
  router ! Broadcast( PoisonPill )

  // call backs
  def receive = {
    case (num: Int, fib: BigInt, startTime : Long) => {
      val processTime = new java.util.Date().getTime() -startTime
      log.info(s"fibonacci for $num is $fib   $processTime")
      router2 ! FibonacciDAO( num, fib, processTime)
    }
    case msg :String => log.info( msg )

    case Terminated(corpse) => {
      if ( corpse == router) {
        log.info("all first stage routees are dead")
        router2 ! Broadcast(PoisonPill)
      }
      else if ( corpse == router2){
        isDead = 1
        log.info( "all second stage routees are dead")
        system.shutdown()
        system.awaitTermination()
      }
    }

    case GET =>{
          // will see this message until system shuts down
          log.info(s"Sending isDead status $isDead")
          sender ! isDead
    }
    case _  => {
      // should never get here
      log.info("no match")
    }
  }
}