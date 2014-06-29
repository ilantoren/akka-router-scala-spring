package sample

import akka.actor._
import org.springframework.scala.context.function._
import akka.routing.{SmallestMailboxRouter, Broadcast}
import akka.pattern.ask
import scala.util._
import sample.FibonacciCollector.GET
<<<<<<< HEAD
=======
import scala.concurrent.Future
>>>>>>> 7a0c3088f4da3efed489616ccd8e3609b2aeb169
import scala.annotation.tailrec


object Main extends App {

  import Config._


<<<<<<< HEAD
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
=======
  val sample = List(10000, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 20, 25, 30, 35, 40, 45, 50, 60, 70, 80, 90, 100, 200, 300, 600)
  val system = ActorSystem("fibonacci")
  val collector = system.actorOf(Props(new FibonacciCollector(sample)), "collector")

  isAlive
>>>>>>> 7a0c3088f4da3efed489616ccd8e3609b2aeb169

  system.shutdown
  system.awaitTermination

<<<<<<< HEAD

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


=======
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
>>>>>>> 7a0c3088f4da3efed489616ccd8e3609b2aeb169


object FibonacciCollector {
  object GET
}

class FibonacciCollector(sample: List[Int]) extends Actor with ActorLogging {

  import FibonacciCollector._

  var list: List[BigInt] = Nil
  var size = sample.size
  var isDead = 0;
<<<<<<< HEAD
  println( s"size of list is $size")
=======
  println(s"size is $size")
>>>>>>> 7a0c3088f4da3efed489616ccd8e3609b2aeb169
  // create a spring context
  implicit val ctx = FunctionalConfigApplicationContext(classOf[AppConfiguration])


  // get hold of the actor system
  val system = ctx.getBean(classOf[ActorSystem])
<<<<<<< HEAD

  // use the Spring Extension to create props for a named actor bean
=======
  // retrieve the properties
>>>>>>> 7a0c3088f4da3efed489616ccd8e3609b2aeb169
  val prop = SpringExtentionImpl(system).props("fibonacciActor")
  val prop2 = SpringExtentionImpl(system).props("slickActor")

  // One way to set routees is to add then to the router in a vector use-case when there is a value needed
  //    in the constructor
  val actor1 = system.actorOf(prop)
  val actor2 = system.actorOf(prop)
  val actor3 = system.actorOf(prop)
  val actor4 = system.actorOf(prop)
  val routees = Vector[ActorRef](actor1, actor2, actor3, actor4)
  val router = system.actorOf(prop.withRouter(SmallestMailboxRouter(routees = routees)))

<<<<<<< HEAD
  // Or more simply
  val router2 = system.actorOf(prop2.withRouter(RoundRobinRouter(nrOfInstances = 5)) )

=======
>>>>>>> 7a0c3088f4da3efed489616ccd8e3609b2aeb169
  for (num <- sample) {
    log.info(s"starting Fibonacci actor with $num")
    router ! num
  }
<<<<<<< HEAD

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
=======
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
>>>>>>> 7a0c3088f4da3efed489616ccd8e3609b2aeb169
      log.info("no match")
    }
  }
}