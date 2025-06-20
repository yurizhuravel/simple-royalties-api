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

`Assumption: upper bound is inclusive, lower bound is not`

and this request:

- (1) 900
- (2) 2000
- (3) 4000

`Assumption: given that the identifier must be a positive integer, as defined by spec above, regard the surrounding brackets in the sample request as a human readability aid. Assume it's an integer in incoming JSON`

the commissions will be:

- (1) 90
- (2) 100
- (3) 40

### Architecture assumptions:
1. The API will mostly be used not by humans but by other services sending valid json in agreed format and expecting a similar response
2. The QA or PROD build will be done automatically on a master push by some agent (Jenkins or similar) which knows how to check out Git repos and handle `sbt` commands, or, alternatively, can run just Docker
3. Service metrics collection is not needed for this MVP
4. We would need to handle the result in a useful way (persist it, or notify some service or a queue etc). Out of scope for this task, so just logging the client ID and total commissions amount in this MVP solution
5. This MVP was not specifically designed to handle big load. Can use more FS2 if this is needed
</details>
-----------

## Description

This is a Scala 3 service that provides a simple API for royalties calculation, adjusting the commission rates depending
on the amount spent. The commission is calculated at the following rates:

| amount            | rate |
|-------------------|------|
| 0 - 1 000         | 10%  |
| 1 000 - 3 000     | 5%   |
| 3 000 - 1 000 000 | 1%   |


## Usage

The service is built and run with `sbt`, which you can get [here](https://www.scala-sbt.org/).

After installing `sbt` and checking out this repository, you can run the tests and the service as follows from the project root:

To run unit tests:

`sbt test`

To run the service: 

`sbt run`

As this is a sample service, it doesn't have a config and doesn't do any environment checks on launch - the base url is `http://0.0.0.0:8080`

TODO for production: env check, base url resolution via `application.conf`

## Running using Docker

Alternatively, you can build and run the service with [Docker](https://www.docker.com/). To do this, after cloning the repo, run the following from the root of this project:

`docker build -t royalties-api --no-cache .`

This will create a `royalties-api` Docker image locally, which you can then run, publishing your `8080` port as follows:

`docker run -p 8080:8080 royalties-api`

Please note that, unlike when running `sbt` directly, `localhost:8080` won't work if you are running the service with Docker - use `0.0.0.0:8080` instead.

## Endpoints

`/health` - health check. Method: `GET`

`/api/commissions` - the main endpoint. Method: `POST`, request body must contain JSON in the following format:

```
{
  "clientId": "client123",
  "serviceCosts": [
    {
      "serviceId": 1,
      "amount": 900
    },
    {
      "serviceId": 2,
      "amount": 2000
    },
    {
      "serviceId": 3,
      "amount": 4000
    }
  ]
}
```

where

  - `clientId`  is a unique client identifier, a string in free format.
  - `serviceCosts` must be a non-empty list of objects each containing:
    - `serviceId` an identifier of a relevant service provided. Must be a positive integer.
    - `amount` a monetary amount payable for this service (a positive integer or Double, which will be rounded by this service to two decimal positions before processing). Must not exceed 1 000 000.

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

For MVP purposes, the service does not handle the result in a way that could be used by other backend services, i.e. persisting to a DB or notifying downstream. The `clientID` and the total amount of commissions payable to that client, however, are logged to the server console.

To test the API locally, start the service and use your favourite HTTP client with JSON in the body as described above, or try a `curl` request in a separate terminal:

```
curl -X POST http://0.0.0.0:8080/api/commissions \
-H "Content-Type: application/json" \
-d '{
"clientId": "The Beatles",
"serviceCosts": [
{"serviceId": 1, "amount": 900},
{"serviceId": 2, "amount": 2000},
{"serviceId": 3, "amount": 4000}
]
}'
```

