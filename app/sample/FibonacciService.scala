package sample

import scala.annotation.tailrec

/**
 * An example service for Spring to inject.  Calculates Fibonacci number using tail recursion
 * Created by Richard Thorne (ilan toren) on 6/18/14.
 */
class FibonacciService  {
  def fibonacci(n: Int) : BigInt ={
    @tailrec def fib( n: Int, a:BigInt, b: BigInt) : BigInt = n match {
      case 0 => a
      case _ => {
        fib(n-1, b, a+b)
      }
    }

    fib(n, 0, 1)
  }
}
