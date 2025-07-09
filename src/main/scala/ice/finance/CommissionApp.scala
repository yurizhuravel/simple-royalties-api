package ice.finance

import cats.effect.{IO, IOApp, Resource}
import org.http4s.HttpRoutes
import org.http4s.dsl.io.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.{Router, Server}
import org.http4s.circe.CirceEntityCodec.*
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import scala.concurrent.duration.*
import com.comcast.ip4s.{ipv4, port}
import pureconfig.ConfigSource
import cats.effect.ExitCode
import natchez.Trace.Implicits.noop

object CommissionApp extends IOApp:
  implicit val logger: Logger[IO] = Slf4jLogger.getLogger[IO]
  val config: Config = ConfigSource.default.at("db").loadOrThrow[Config]

  val commissionEndpoints: HttpRoutes[IO] =
    HttpRoutes.of[IO] { case req @ POST -> Root / "commissions" =>
      req.as[ClientRequest].flatMap { clientReq =>
        logger.info(s"Received a request from client ${clientReq.clientId}") *>
          Handlers.processRequest(config, clientReq).flatMap {
            case Right(results) => 
              Ok(results)
            case Left(error) =>
              BadRequest(error)
          }
        }
        .handleErrorWith(Handlers.handleClientRequestError(using logger))
    }

  val healthEndpoint: HttpRoutes[IO] =
    HttpRoutes.of[IO] { case GET -> Root =>
      Ok("Service is healthy!")
    }

  val router = Router(
    "/api"    -> commissionEndpoints,
    "/health" -> healthEndpoint
  ).orNotFound

  private def createServer(implicit logger: Logger[IO]): Resource[IO, Server] =
    EmberServerBuilder
    .default[IO]
    .withHttpApp(router)
    .withHost(ipv4"0.0.0.0")
    .withPort(port"8080")
    .withShutdownTimeout(1.second)
    .build

  override def run(args: List[String]): IO[ExitCode] =
    logger.info("Starting ICE Commission Calculation API") *>
      createServer(logger).use(_ => logger.info("Server is up and running") >> IO.never)

end CommissionApp
