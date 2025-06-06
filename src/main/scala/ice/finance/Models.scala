package ice.finance

case class AmountPerService(serviceId: Int, amount: Double)
case class ClientRequest(clientId: String, serviceCosts: List[AmountPerService])