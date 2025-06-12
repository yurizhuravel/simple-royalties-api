package ice.finance

import cats.effect.IO
import cats.implicits.toTraverseOps
import fs2.Stream
import org.typelevel.log4cats.Logger

object CommissionService:
  def calculateRate(amount: Double): Double = amount match
    case a if a < 1000 => 0.1
    case a if a < 3000 => 0.05
    case _ => 0.01

  // Truncate to two decimal places because it's currency
  def toCurrencyFormat(d: Double): Double =
    BigDecimal(d)
      .setScale(2, BigDecimal.RoundingMode.HALF_UP)
      .toDouble

  // Calculate commission for a single service
  private def calculateCommission(serviceCost: ServiceCost)(implicit logger: Logger[IO]): IO[ServiceCommission] = {
    val amount = serviceCost.amount
    val commission = toCurrencyFormat(amount * calculateRate(amount))

    IO.pure(ServiceCommission(serviceCost.serviceId, commission))
  }

  def totalCommission(results: List[ServiceCommission]): Double = toCurrencyFormat(results.map(_.commission).sum)

  // Validate a client request
  private def validateRequest(request: ClientRequest)(implicit logger: Logger[IO]): IO[Boolean] =
    val amountIsInvalid = request.serviceCosts.exists(sc => sc.amount > 1000000 || sc.amount < 0)
    val serviceIdIsInvalid = request.serviceCosts.exists(sc => sc.serviceId <= 0)

    request.serviceCosts match
      case Nil => 
        logger.error("Could not process the request: Services list cannot be empty") *>
        IO.pure(false)
      case _ if amountIsInvalid =>
        logger.error(s"Could not process request: Amount per service must be between 0 and 1 000 000") *>
        IO.pure(false)
      case _ if serviceIdIsInvalid =>
        logger.error(s"Could not process request: Service ID must be a positive integer") *>
        IO.pure(false)
      case _ =>
        IO.pure(true)

  def processRequest(request: ClientRequest)(implicit logger: Logger[IO]): IO[List[ServiceCommission]] =
    validateRequest(request).flatMap {
      case false => IO.pure(List.empty)
      case true =>
        val result = request.serviceCosts.traverse(calculateCommission)
        logger.info(s"Successfully processed request from client ${request.clientId}") *>
        result
    }
end CommissionService