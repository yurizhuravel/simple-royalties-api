package ice.finance

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

trait AmountPerService

case class ServiceCost(serviceId: Int, amount: Double) extends AmountPerService

object ServiceCost:
  implicit val serviceCostEncoder: Encoder[ServiceCost] = deriveEncoder[ServiceCost]
  implicit val serviceCostDecoder: Decoder[ServiceCost] = deriveDecoder[ServiceCost]

case class ServiceCommission(serviceId: Int, commission: Double) extends AmountPerService

object ServiceCommission:
  implicit val serviceCommissionEncoder: Encoder[ServiceCommission] = deriveEncoder[ServiceCommission]
  implicit val serviceCommissionDecoder: Decoder[ServiceCommission] = deriveDecoder[ServiceCommission]

case class ClientRequest(clientId: String, serviceCosts: List[ServiceCost])

object ClientRequest:
  implicit val clientRequestEncoder: Encoder[ClientRequest] = deriveEncoder[ClientRequest]
  implicit val clientRequestDecoder: Decoder[ClientRequest] = deriveDecoder[ClientRequest]