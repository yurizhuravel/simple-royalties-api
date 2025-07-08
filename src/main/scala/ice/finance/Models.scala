package ice.finance

import io.circe.{Decoder, Encoder, Json}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import pureconfig.ConfigReader
import pureconfig.generic.derivation.default.*

trait AmountPerService

case class ServiceCost(serviceId: Int, amount: Double) extends AmountPerService

object ServiceCost:
  implicit val serviceCostEncoder: Encoder[ServiceCost] = deriveEncoder
  implicit val serviceCostDecoder: Decoder[ServiceCost] = deriveDecoder

case class ServiceCommission(serviceId: Int, commission: Double) extends AmountPerService

object ServiceCommission:
  implicit val serviceCommissionEncoder: Encoder[ServiceCommission] = deriveEncoder
  implicit val serviceCommissionDecoder: Decoder[ServiceCommission] = deriveDecoder

case class ClientRequest(clientId: String, serviceCosts: List[ServiceCost])

object ClientRequest:
  implicit val clientRequestEncoder: Encoder[ClientRequest] = deriveEncoder
  implicit val clientRequestDecoder: Decoder[ClientRequest] = deriveDecoder

final case class Config (
  host: String,
  port: Int,
  username: String,
  password: String,
  database: String
) derives ConfigReader

enum ValidationError(val message: String):
  case EmptyServiceList extends ValidationError("Services list cannot be empty")
  case InvalidAmount extends ValidationError("Amount per service must be between 0 and 1,000,000")
  case InvalidServiceId extends ValidationError("Service ID must be a positive integer")

object ValidationError:
  implicit val validationErrorEncoder: Encoder[ValidationError] = Encoder.instance { error =>
    Json.obj("error" -> Json.fromString(error.message))
  }