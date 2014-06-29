package sample
/*
 http://typesafe.com/activator/template/akka-scala-spring
 */
import akka.actor.ActorSystem
import org.springframework.context.ApplicationContext
import org.springframework.scala.context.function.FunctionalConfiguration
import org.springframework.beans.factory.config.BeanDefinition



class AppConfiguration extends FunctionalConfiguration {
  /**
   * Load implicit context
   */
  implicit val ctx = beanFactory.asInstanceOf[ApplicationContext]

  /**
   * Actor system singleton for this application.
   */
  val actorSystem = bean() {
    val system = ActorSystem("AkkaScalaSpring")
    // initialize the application context in the Akka Spring Extension
    SpringExtentionImpl(system)
    system
  }

  val fibonacciService = bean("fibonacciService") {
    new FibonacciService
  }

  val slickService = bean("slickService") {
    println( "creating the SlickServiceBean")
    val sl = new SlickService

    sl.start
    Main.slickService = sl
    sl
  }

  val fibonacciActor = bean("fibonacciActor", scope = BeanDefinition.SCOPE_PROTOTYPE) {
    val fn = new FibonacciActor
    fn.fibonacciService = fibonacciService()
    fn
  }

  val slickActor = bean("slickActor", scope =  BeanDefinition.SCOPE_PROTOTYPE ) {
    val sa = new SlickActor
    sa.slickService = slickService()
    sa
  }
}