package ice.finance

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

object JsonCodecs:
  
  implicit val amountPerServiceEncoder: Encoder[AmountPerService] = deriveEncoder[AmountPerService]
  implicit val amountPerServiceDecoder: Decoder[AmountPerService] = deriveDecoder[AmountPerService]
  
  implicit val clientRequestEncoder: Encoder[ClientRequest] = deriveEncoder[ClientRequest]
  implicit val clientRequestDecoder: Decoder[ClientRequest] = deriveDecoder[ClientRequest]
  
end JsonCodecs
