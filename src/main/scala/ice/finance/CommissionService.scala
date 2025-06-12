package ice.finance

import cats.effect.IO
import fs2.Stream
import org.typelevel.log4cats.Logger

object CommissionService:
  // Calculate commission rate based on amount
  private def calculateRate(amount: Double): Double =
    if amount <= 1000 then 0.1
    else if amount <= 3000 then 0.05
    else 0.01

  // Truncate to two decimal places because it's currency
  private def toCurrencyFormat(d: Double): Double =
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

  def processRequest(request: ClientRequest)(implicit logger: Logger[IO]): IO[List[ServiceCommission]] =
    request match {
      case req if req.serviceCosts.isEmpty =>
        logger.error("Services list cannot be empty") *> IO.pure(List.empty)

      case req if req.serviceCosts.exists(s => s.amount > 1000000 || s.amount < 0) =>
        logger.error("Amount per service must be between 0 and 1 000 000") *> IO.pure(List.empty)

      case req if req.serviceCosts.exists(s => s.serviceId <= 0) =>
        logger.error("Service ID must be a positive integer") *> IO.pure(List.empty)

      case _ =>
        // Process in parallel
        Stream.emits(request.serviceCosts)
          .covary[IO]
          .parEvalMap(4)(calculateCommission(_))
          .compile
          .toList
    }
end CommissionService