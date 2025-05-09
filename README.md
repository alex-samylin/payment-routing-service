## Description

The service exposes a `POST /payments` endpoint to create payments.
Incoming requests include payment details such as amount, currency, and card number.
The service selects a payment provider based on routing logic, forwards the request to that provider, handles the response, and returns the result to the client.

Contracts with payment providers are a critical part of the architecture. 
Each provider accepts requests in a defined data format and returns either a success response or an error. 
Basic request and response structures have been specified, along with agreed error codes. 
The service interprets provider errors and maps them to its own business exceptions.

## Routing and Providers

Routing logic is implemented in the PaymentProviderFactory class.
Currently, there are two mock providers: ProviderA and ProviderB.
The routing condition is as follows:
If the card's BIN (first digits of the card number) starts with "42", the currency is "USD", and the amount is less than 100000 — then ProviderA is used.
In all other cases, ProviderB is used.

Each provider is implemented as a separate client class (ProviderAClient, ProviderBClient) and sends HTTP requests to an external URL.
All external calls are stubbed in tests using MockRestServiceServer.

## Retry Logic

Retry logic is configured externally using Resilience4j in the `application.properties` file.
Each provider can have its own retry configuration.
Retries are triggered for technical exceptions like `HttpServerErrorException.ServiceUnavailable`, `HttpClientErrorException.TooManyRequests`, or `ResourceAccessException`.
Business errors such as `409 Conflict` or `400 Bad Request` are excluded from retries.
`ignore-exceptions` can be configured to explicitly skip retry for specific exception classes.

## Error Handling

The application defines two categories of errors:

1. **Business Errors**: Returned by providers for issues related to input data (e.g., expired card). These are mapped to HTTP 409 responses.
2. **Technical Errors**: Timeouts, server errors, or provider unavailability. These are mapped to HTTP 502 Bad Gateway.

Error handling is centralized using a `@RestControllerAdvice`.

## Idempotency

Each request to a provider includes a unique `transactionId` generated via `UUID.randomUUID()`.
This ID helps providers identify duplicate requests and ensures idempotency in case of retries.

## Testing

The project uses MockMvc and MockRestServiceServer for integration testing.
All tests are located in the PaymentControllerTest class and cover the full flow from the controller to external provider calls.

The tests were intentionally kept minimal, in accordance with the project task ("Includes minimal tests"). 
The selected test cases cover the most important functional areas of the service:

* Successful payment creation
* Provider selection (routing logic)
* Retry logic on 503 errors
* Business error handling (e.g., 409 Conflict)
* Technical error handling (e.g., 502 Bad Gateway)

## OpenAPI / Swagger

The project integrates `springdoc-openapi` for automatic API documentation.
Swagger UI is available at `/swagger-ui.html`.

## Extensibility

The service is designed to easily accommodate new providers.
To add a new provider, implement a new client class and register it in the `PaymentProviderFactory`.
Retry rules and error handling can also be extended or customized using configuration.

## AI and Reliability Considerations

AI can be used to adapt retry logic dynamically based on real-time analysis of provider responses.
For example, if a specific type of error starts occurring more frequently, 
the system could temporarily increase the timeout or 
reduce the retry frequency to avoid overloading the provider or triggering rate limits.

## Scalability Considerations

### 1. Horizontal Scaling
Run multiple instances of the service and use load balancing to distribute incoming traffic. 
This allows the system to handle increased request volume efficiently.

### 2. Retry Control
Use limited retries with exponential backoff and a small randomized delay to avoid overloading providers. 
Introduce a circuit breaker to temporarily stop requests to unstable providers.

### 3. Asynchronous Processing
If real-time response is not critical, send payment requests to a message queue for background processing.
This improves reliability and system throughput under high load.
