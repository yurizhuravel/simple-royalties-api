package ice.finance

import cats.effect.{IO, IOApp, Resource}
import cats.implicits.*
import org.http4s.{Http, HttpRoutes}
import org.http4s.dsl.io.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.{Server, Router}
import org.http4s.circe.CirceEntityCodec._
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import scala.concurrent.duration.*
import ice.finance.CommissionService.*

object App extends IOApp.Simple:
  implicit val logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  val commissionEndpoints: HttpRoutes[IO] = {
    HttpRoutes.of[IO] {
      
      // case GET -> Root / "commissions" =>

      case req @ POST -> Root / "commissions" =>
        for {
          clientRequest <- req.as[ClientRequest]
          clientId      = clientRequest.clientId
          _             <-  logger.info(s"Received a request from client $clientId")
          results       <- CommissionService.processRequest(clientRequest)
          response      <- Ok(results)
        } yield response
    }
  }

  val healthEndpoint: HttpRoutes[IO] = {
    HttpRoutes.of[IO] {

      case GET -> Root =>
        Ok("Service is healthy!")
    }
  }

  val router = Router(
    "/api" -> commissionEndpoints,
    "/health" -> healthEndpoint
  ).orNotFound

  private def createServer(implicit logger: Logger[IO]): Resource[IO, Server] = {
    EmberServerBuilder
      .default[IO]
      .withHttpApp(router)
      .build
  }

  override def run: IO[Unit] = {
    for {
      _ <- logger.info("Starting ICE Commission Calculation API")
      _ <- createServer(logger).use(_ => logger.info("Server is up and running") *> IO.never)
    } yield ()
  }

end App
