package ice.finance

import cats.effect.{IO, Resource}
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.{Router, Server}
import org.typelevel.log4cats.Logger
import com.comcast.ip4s.*
import scala.concurrent.duration._

object Server:
  def create(implicit logger: Logger[IO]): Resource[IO, Server] = {
    val commissionRoutes = new Routes().routes

    val httpApp = Router(
      "/api" -> commissionRoutes
    ).orNotFound

    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(httpApp)
      .withIdleTimeout(Duration(1, MINUTES))
      .build
  }
end Server
