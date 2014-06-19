package sample


class TestAppConfiguration extends AppConfiguration {

  override val fibonacciService = bean("fibonacciService") {
    val cs :  FibonacciService =  new FibonacciService
    cs
    }
}
