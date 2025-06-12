package ice.finance

import cats.data.Kleisli
import cats.effect.{IO, IOApp, Resource}
import org.http4s.{HttpRoutes, MessageFailure, Request, Response}
import org.http4s.dsl.io.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.{Router, Server}
import org.http4s.circe.CirceEntityCodec.*
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import scala.concurrent.duration.*

object CommissionApp extends IOApp.Simple:
  implicit val logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  val commissionEndpoints: HttpRoutes[IO] = {
    HttpRoutes.of[IO] {
      case req @ POST -> Root / "commissions" =>
        req.as[ClientRequest].flatMap { clientReq =>
          logger.info(s"Received a request from client ${clientReq.clientId}") *>
            Handlers.processRequest(clientReq).flatMap {
              case Right(results) => 
                Ok(results)
              case Left(error) => 
                BadRequest(error)
      }
        }.handleErrorWith {
          case e: MessageFailure =>
            logger.error(s"Failed to parse client request: ${e.getMessage}") *>
              BadRequest("Invalid request - please check your request is not malformed and conforms to the format expected by the API")
          case e =>
            logger.error(s"Some weirdness happening: ${e.getMessage}") *>
              InternalServerError("An unexpected error occurred")
        }
    }
  }

  val healthEndpoint: HttpRoutes[IO] = {
    HttpRoutes.of[IO] {
      case GET -> Root =>
        Ok("Service is healthy!")
    }
  }

  val router: Kleisli[IO, Request[IO], Response[IO]] = Router(
    "/api" -> commissionEndpoints,
    "/health" -> healthEndpoint
  ).orNotFound

  private def createServer(implicit logger: Logger[IO]): Resource[IO, Server] = {
    EmberServerBuilder
      .default[IO]
      .withHttpApp(router)
      .withShutdownTimeout(1.second)
      .build
  }

  override def run: IO[Unit] = {
    for {
      _ <- logger.info("Starting ICE Commission Calculation API")
      _ <- createServer(logger).use(_ => logger.info("Server is up and running") >> IO.never)
    } yield ()
  }

end CommissionApp
