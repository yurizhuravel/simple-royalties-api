package ice.finance

import weaver._
import cats.syntax.all._
import io.circe.syntax._
import io.circe.parser.decode

object ModelsSpec extends FunSuite {

  test("ServiceCost codec should encode and decode properly") {
    val serviceCost = ServiceCost(1, 100)
    val json        = serviceCost.asJson.noSpaces
    val decoded     = decode[ServiceCost](json)

    expect(decoded == Right(serviceCost))
  }

  test("ServiceCost should decode from valid JSON") {
    val json     = """{"serviceId":2,"amount":50.50}"""
    val expected = ServiceCost(2, 50.50)
    val decoded  = decode[ServiceCost](json)

    expect(decoded == Right(expected))
  }

  test("ServiceCommission codec should encode and decode properly") {
    val commission = ServiceCommission(3, 15.5)
    val json       = commission.asJson.noSpaces
    val decoded    = decode[ServiceCommission](json)

    expect(decoded == Right(commission))
  }

  test("ServiceCommission should decode from valid JSON") {
    val json     = """{"serviceId":4,"commission":42}"""
    val expected = ServiceCommission(4, 42.0)
    val decoded  = decode[ServiceCommission](json)

    expect(decoded == Right(expected))
  }

  test("ClientRequest codec should encode and decode properly") {
    val costs   = List(ServiceCost(5, 30.0), ServiceCost(6, 150.75))
    val request = ClientRequest("client123", costs)
    val json    = request.asJson.noSpaces
    val decoded = decode[ClientRequest](json)

    expect(decoded == Right(request))
  }

  test("ClientRequest should decode from valid JSON") {
    val json =
      """{"clientId":"client123","serviceCosts":[{"serviceId":7,"amount":420},{"serviceId":8,"amount":180.5}]}"""
    val expected = ClientRequest("client123", List(ServiceCost(7, 420.0), ServiceCost(8, 180.5)))
    val decoded  = decode[ClientRequest](json)

    expect(decoded == Right(expected))
  }

  test("JSON decoding should fail with invalid field types") {
    val invalidJson = """{"serviceId":"not-a-number","amount":100.0}"""
    val result      = decode[ServiceCost](invalidJson)

    expect(result.isLeft)
  }
}
