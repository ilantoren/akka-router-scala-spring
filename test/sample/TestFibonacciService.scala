package sample

import scala.concurrent.future

import akka.actor.ActorRef
import org.springframework.scala.context.function.FunctionalConfigApplicationContext

import akka.actor.ActorSystem

import akka.pattern.ask
import org.springframework.scala.context.function._
import org.specs2.mutable._
import org.junit.runner._
import org.specs2.runner._

@RunWith(classOf[JUnitRunner])
class TestFibonacciService extends Specification{


  "Simple test" should {

    "Test FibonacciService"    in  {
      // create a spring context
      implicit val ctx = FunctionalConfigApplicationContext(classOf[TestAppConfiguration])

      import Config._
      // get hold of the actor system
      val fibonacciService = ctx.getBean(classOf[FibonacciService])
      val result : BigInt = fibonacciService.fibonacci(40)

      result must beEqualTo(102334155)


    }
  }

}