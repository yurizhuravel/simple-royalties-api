package ice.finance

import cats.effect.{IO, IOApp}
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.log4cats.Logger

object Main extends IOApp.Simple:
  implicit val logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  override def run: IO[Unit] = {
    for {
      _ <- logger.info("Starting ICE Commission Calculation API")
      _ <- Server.create(logger).use(_ => IO.never)
    } yield ()
  }
end Main
