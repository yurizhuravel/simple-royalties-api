package ice.finance

import cats.effect.IO
import cats.implicits.toTraverseOps
import org.typelevel.log4cats.Logger
import org.http4s.dsl.io.*
import org.http4s.{Response, MessageFailure}
import ValidationError.*

object Handlers:
  def calculateRate(amount: Double): Double = amount match
    case a if a < 1000 => 0.1
    case a if a < 3000 => 0.05
    case _             => 0.01

  // Truncate to two decimal places because it's currency
  def toCurrencyFormat(d: Double): Double =
    BigDecimal(d)
      .setScale(2, BigDecimal.RoundingMode.HALF_UP)
      .toDouble

  // Calculate commission for a single service
  private def calculateCommission(
      serviceCost: ServiceCost
  )(implicit logger: Logger[IO]): IO[ServiceCommission] = {
    val amount     = serviceCost.amount
    val commission = toCurrencyFormat(amount * calculateRate(amount))

    IO.pure(ServiceCommission(serviceCost.serviceId, commission))
  }

  def totalCommission(commissionsList: List[ServiceCommission]): Double = toCurrencyFormat(
    commissionsList.map(_.commission).sum
  )

  private def validateRequest(request: ClientRequest): Either[ValidationError, Unit] =
    val amountIsInvalid    = request.serviceCosts.exists(sc => sc.amount > 1000000 || sc.amount < 0)
    val serviceIdIsInvalid = request.serviceCosts.exists(sc => sc.serviceId <= 0)

    request.serviceCosts match
      case Nil                     => Left(EmptyServiceList)
      case _ if amountIsInvalid    => Left(InvalidAmount)
      case _ if serviceIdIsInvalid => Left(InvalidServiceId)
      case _                       => Right(())

  def processRequest(request: ClientRequest)(implicit
      logger: Logger[IO]
  ): IO[Either[ValidationError, List[ServiceCommission]]] =
    validateRequest(request) match
      case Left(error) =>
        logger.error(s"Validation error: ${error.message}") *>
          IO.pure(Left(error))
      case Right(_) =>
        request.serviceCosts.traverse(calculateCommission).flatMap { commissions =>
          val total = totalCommission(commissions)
          logger
            .info(
              s"Successfully processed request from client ${request.clientId}, total commission: $total"
            )
            .as(Right(commissions))
        }

  def handleClientRequestError(using logger: Logger[IO])(error: Throwable): IO[Response[IO]] =
    error match
      case e: MessageFailure =>
        logger.error(s"Failed to parse client request: ${e.getMessage}") *>
          BadRequest(
            "Invalid request - please check your request is not malformed and conforms to the format expected by the API"
          )
      case e =>
        logger.error(s"Some weirdness happening: ${e.getMessage}") *>
          InternalServerError("An unexpected error occurred")

end Handlers
