package ice.finance

import weaver._
import cats.syntax.all._

object HandlersSpec extends FunSuite:
  
  test("calculateRate should respect upper/lower bounds and return correct rates") {
    expect(Handlers.calculateRate(500) == 0.1) &&
    expect(Handlers.calculateRate(1000) == 0.05) &&
    expect(Handlers.calculateRate(2000) == 0.05) &&
    expect(Handlers.calculateRate(3000) == 0.01) &&
    expect(Handlers.calculateRate(5000) == 0.01)
  }

  test("toCurrencyFormat should truncate to two decimal places") {
    expect(Handlers.toCurrencyFormat(123.456) == 123.46) &&
    expect(Handlers.toCurrencyFormat(123.4) == 123.40) &&
    expect(Handlers.toCurrencyFormat(123) == 123.00)
  }

  test("totalCommission should calculate sum of commissions correctly") {
    val commissions = List(
      ServiceCommission(1, 10.5),
      ServiceCommission(2, 11.5),
      ServiceCommission(3, 20)
    )
    expect(Handlers.totalCommission(commissions) == 42)
  }

end HandlersSpec