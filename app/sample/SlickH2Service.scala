package sample


import scala.slick.driver.H2Driver.simple._
import scala.slick.direct.AnnotationMapper.column
import scala.slick.model.Column
import scala.collection.mutable


/**
 * Created by Richard Thorne (ilan toren) on 6/26/14.
 *  An injectable service to perist data to H2 using the Slick library
 * */


case class FibonacciDAO(num: Int, fibonacci: BigInt, processtime: Long)

class SlickService {


  // Definition of the Fibonacci table
  class Fibonacci(tag: Tag) extends Table[(Int, String, Long)](tag, "FIBONACCI") {

    def nvalue = column[Int]("FIB_N")

    def fvalue = column[String]("FIB_VAL")

    def processTime = column[Long]("FIB_PROCESS_TIME")

    // Every table needs a * projection with the same type as the table's type parameter
    def * = (nvalue, fvalue, processTime)
  }

  def start: Boolean = {
    val fibonacci = TableQuery[Fibonacci]
    // Connect to the database and execute the following block within a session
    // DB_CLOSE_DELAY=-1 keeps the H2 alive until JVM shuts down
    Database.forURL("jdbc:h2:mem:test1;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver") withSession {
      implicit session =>

        // Create the tables, including primary and foreign keys
        (fibonacci.ddl).create

    }
    true
  }

  def save(num: Int, fibnum: BigInt, ptime: Long) {
    val fibonacci = TableQuery[Fibonacci]
    // Connect to the database and execute the following block within a session
    Database.forURL("jdbc:h2:mem:test1", driver = "org.h2.Driver") withSession {


      implicit session =>
        fibonacci +=(num, fibnum.toString, ptime)
    }
  }

  def find(n: Int): Option[FibonacciDAO] = {
    val fibonacci = TableQuery[Fibonacci]

    Database.forURL("jdbc:h2:mem:test1", driver = "org.h2.Driver") withSession {
      implicit session =>
        val res = for {
          f <- fibonacci if f.nvalue === n
        } yield f

        val inter = res mapResult {
          case (nvalue, fvalue, processTime) => Option(FibonacciDAO(nvalue, BigInt.apply(fvalue), processTime))
        }

        val result = inter.list match {
          case _ :: tail => inter.first
          case Nil => None
        }
        result
    }
  }

  private def mconv( x : (Int, String, Long) )  : List[FibonacciDAO] = {
    // convert a single row to a list of 1 row
     FibonacciDAO( x._1, BigInt.apply(x._2), x._3)::Nil
  }


  def findAll() : List[FibonacciDAO] = {
    val lst : mutable.MutableList[FibonacciDAO] = new mutable.MutableList()
    val fibonacci = TableQuery[Fibonacci]
    Database.forURL("jdbc:h2:mem:test1", driver = "org.h2.Driver") withSession {
      implicit session =>
        /*
        This is the format for filtering the results
        val res = for {
          f <- fibonacci if f.? > ?
        }yield ( f   )

        res.foreach( x =>  lst ++= mconv(x) )
        */
        fibonacci.foreach( x =>  lst ++= mconv(x))
    }
    lst.toList
  }
}
