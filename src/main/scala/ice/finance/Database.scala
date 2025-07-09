package ice.finance

import ice.finance.Config
import cats.effect.{Resource, Temporal, IO}
import cats.effect.std.Console
import cats.syntax.all.*
import fs2.io.net.Network
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import skunk.*
import skunk.syntax.all.*
import skunk.codec.all.*
import natchez.Trace
import natchez.Trace.Implicits.noop
import cats.Monad
import cats.implicits.toTraverseOps

object Database:
  implicit val logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  def getSession[F[_] : Temporal : Trace : Network: Console](config: Config): Resource[F, Session[F]] = 
    Session.single(
      host = config.host,
      port = config.port,
      user = config.username,
      password = Some(config.password),
      database = config.database,
    )
  
  val commissionCodec: Codec[ClientCommission] = 
    (varchar, float8).tupled.imap {
      case (clientId, commission) => ClientCommission(clientId, commission)
      } {
      case ClientCommission(clientId, commission) => (clientId, commission)
      }

  def upsert[F[_]: Monad: Logger](session: Session[F], clientTotal: ClientCommission): F[Unit] =
    for {
      command <- session.prepare(sql"""
        INSERT INTO commissions (client_id, commission) 
        VALUES ($commissionCodec)
        ON CONFLICT (client_id) 
        DO UPDATE SET commission = commissions.commission + EXCLUDED.commission
        """.command)
      rowCount <- command.execute(clientTotal)
      _ <- Logger[F].info(s"Adding total commission of ${clientTotal.commission} for client ${clientTotal.clientId} to the database")
    } yield ()

  def updateCommission[F[_]](config: Config, clientTotal: ClientCommission) =
    getSession[IO](config).use { session =>
      upsert(session, clientTotal)
    }