# Simple Royalties Service

----------------

## Context

This is a test assignment
<details>
  <summary>Click to see the task description with my assumption notes</summary>

(My assumptions are in `code blocks`)

Imagine you work for a company that offers an intermediary service and you are part of the team responsible for
calculating commissions for clients.

- You will receive a request that contains a non empty list of services we provide
  `MVP Assumption: the request is an HTTP POST containing JSON with the agreed schema in the request body. TODO: use a better thing, like Kafka or something`
    - Each element in the list will contain
        - an identifier, a positive integer
        - total amount of money, that cannot be greater than 1,000,000
          `Assumption: treat input as a Double with two decimal places precision because it is currency, regard the comma separator as a human readability aid`
    - Each request is associated with a specific client
      `MVP Assumption: user login and management was already handled upstream, we are receiving a unique client ID in the request`
    - Our company will charge a percentage of the total amount of each element from the list
    - In order to calculate the rate associated with a specific element on the list we will use the amount.

For example:

Given this commission
| amount | rate |
|---------------------|------------|
| 0 - 1,000 | 10 % |
| 1,000 - 3,000 | 5% |
| 3,000 - 1,000,000 | 1% |

and this request:

- (1) 900
- (2) 2000
- (3) 4000

`Assumption: given that the identifier must be a positive integer, as defined by spec above, regard the surrounding brackets in the sample request as a human readability aid. Assume it's an integer in incoming JSON`

the commissions will be:

- (1) 90
- (2) 100
- (3) 40

`TODO Assumption: we need to persist it or notify someone or do something. Out of scope for this task, so just logging it out in the solution`
</details>

-----------

## Design notes

This is a Scala 3 service that provides a simple API for royalties calculation, adjusting the commission rates depending
on the amount spent. The commission is calculated at the following rates:

| amount            | rate |
|-------------------|------|
| 0 - 1 000         | 10%  |
| 1 000 - 3 000     | 5%   |
| 3 000 - 1 000 000 | 1%   |


The service is built and run with `sbt`, which you can get [here](https://www.scala-sbt.org/). Once installed, start the service with 

`sbt run`

## Endpoints

`/api/health` - health check. Accepts a simple HTTP `GET`

`/api/commissions` - the main endpoint. Accepts HTTP `POST` requests containing a JSON body in the following format:

```
{
  "clientID": "client123",
  "serviceCosts": [
    {
      "serviceID": 1,
      "amount": 900
    },
    {
      "serviceID": 2,
      "amount": 2000
    },
    {
      "serviceID": 3,
      "amount": 4000
    }
  ]
}
```

where

- `clientID` is a unique client identifier
- `serviceID` is an integer identifier of a relevant service provided, and
- `amount` is a monetary amount payable for this service

Expected response for the sample JSON would be:

```
[
  {
    "serviceId": 1,
    "amount": 90
  },
  {
    "serviceId": 2,
    "amount": 100
  },
  {
    "serviceId": 3,
    "amount": 40
  }
]
```

To test the API locally, you can use your favourite HTTP client with JSON in the body as described above, or try a `curl` request:

```
curl -X POST http://localhost:8080/api/commissions \
-H "Content-Type: application/json" \
-d '{
"clientId": "Ed Sheeran",
"serviceCosts": [
{"serviceId": 1, "amount": 900},
{"serviceId": 2, "amount": 2000},
{"serviceId": 3, "amount": 4000}
]
}'
```

