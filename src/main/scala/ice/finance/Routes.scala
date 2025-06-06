package ice.finance

import cats.effect.IO
import org.http4s.HttpRoutes
import org.http4s.dsl.io.*
import org.http4s.circe.CirceEntityCodec._
import org.typelevel.log4cats.Logger
import JsonCodecs.*
import CommissionService.*

class Routes(implicit logger: Logger[IO]):
  val routes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case req @ POST -> Root / "commissions" =>
      req.as[ClientRequest].flatMap { clientRequest =>
        CommissionService.processRequest(clientRequest).flatMap { results =>
          logger.info(s"Successfully processed request from client ${clientRequest.clientId} for the total of ${totalCommission(results)}") *>
            Ok(results)
        }
      }.handleErrorWith {
        case e: IllegalArgumentException =>
          logger.error(e)(s"Validation error") *> BadRequest(e.getMessage)
        case e =>
          logger.error(e)(s"Server error") *> InternalServerError(s"An unexpected error occurred: ${e.getMessage}")
      }

    // Health check endpoint
    case GET -> Root / "health" => Ok("Service is healthy")
  }
end Routes