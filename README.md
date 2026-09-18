# Midas Core Banking System

An event-driven banking backend built with **Spring Boot, PostgreSQL, Apache Kafka, and the Transactional Outbox pattern**.

This project is an **independent implementation inspired by concepts explored in the JPMorgan Chase Midas Core Forage simulation**. The codebase, architecture, business logic, and persistence model were implemented separately as a portfolio project.

## Overview

Midas Core Banking System provides a small but realistic banking backend that supports account management, deposits, withdrawals, transfers, transaction history, and asynchronous transfer rewards.

The main design goal is to demonstrate how a transactional REST backend can publish reliable domain events without tightly coupling the banking transaction to downstream processing.

## Architecture

```text
                         ┌─────────────────────┐
                         │     REST Client     │
                         │ Swagger / Postman   │
                         └──────────┬──────────┘
                                    │
                                    ▼
                         ┌─────────────────────┐
                         │   Spring Boot API   │
                         │ Controllers / DTOs  │
                         └──────────┬──────────┘
                                    │
                                    ▼
                         ┌─────────────────────┐
                         │    AccountService   │
                         │ Business Logic      │
                         └───────┬───────┬─────┘
                                 │       │
                      transaction│       │event
                                 ▼       ▼
                      ┌──────────────┐  ┌─────────────────┐
                      │ PostgreSQL   │  │ Transactional   │
                      │ Accounts     │  │ Outbox          │
                      │ Transactions │  │ outbox_event    │
                      │ Rewards      │  └────────┬────────┘
                      └──────────────┘           │
                                                 │
                                                 ▼
                                      ┌─────────────────────┐
                                      │   Outbox Publisher  │
                                      └──────────┬──────────┘
                                                 │
                                                 ▼
                                      ┌─────────────────────┐
                                      │   Apache Kafka      │
                                      │   banking-events    │
                                      └──────────┬──────────┘
                                                 │
                                                 ▼
                                      ┌─────────────────────┐
                                      │  Kafka Consumer     │
                                      │  IncentiveService   │
                                      └──────────┬──────────┘
                                                 │
                                                 ▼
                                      ┌─────────────────────┐
                                      │ Reward Calculation   │
                                      │ 1% transfer reward  │
                                      │ Maximum ₹100        │
                                      └─────────────────────┘
```

## Event Flow

A transfer follows this sequence:

1. The client calls `POST /api/accounts/transfer`.
2. `AccountService` validates the accounts and available balance.
3. Sender and recipient balances are updated in one database transaction.
4. A `BankTransaction` record is created.
5. A corresponding `OutboxEvent` is written in the same database transaction.
6. `OutboxPublisher` publishes pending events to Kafka.
7. `KafkaConsumerService` receives the event.
8. `IncentiveService` calculates and persists the reward.
9. The reward is credited back to the sender's account.

This keeps the core banking transaction independent from the asynchronous incentive workflow.

## Features

### Account Management

- Create an account with an initial balance
- Retrieve one account or all accounts
- Unique email constraint
- Optimistic locking using JPA `@Version`

### Money Operations

- Deposit money
- Withdraw money
- Transfer money between accounts
- Reject insufficient-balance operations
- Reject self-transfers
- Use `BigDecimal` for monetary values

### Transaction History

- Persist completed banking transactions
- Retrieve transaction history for an account
- Track transaction type, amount, status, timestamps, and counterpart account

### Event-Driven Processing

- Apache Kafka event publishing
- Kafka consumer for downstream processing
- `banking-events` topic
- Transactional Outbox pattern
- Pending outbox events are published asynchronously

### Incentive System

Completed transfers receive:

```text
Reward = 1% of transfer amount
Maximum reward = ₹100
```

Rewards are stored separately and protected against duplicate processing using a unique transaction constraint.

### API Error Handling

The application includes centralized exception handling for:

- Validation errors
- Missing accounts
- Insufficient balance
- Self-transfers
- Account conflicts
- Optimistic locking conflicts
- Database constraint violations

## REST API

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/accounts` | Create an account |
| GET | `/api/accounts` | List all accounts |
| GET | `/api/accounts/{id}` | Get an account |
| POST | `/api/accounts/{id}/deposit` | Deposit money |
| POST | `/api/accounts/{id}/withdraw` | Withdraw money |
| POST | `/api/accounts/transfer` | Transfer money |
| GET | `/api/accounts/{id}/transactions` | Get transaction history |
| GET | `/api/rewards/account/{accountId}` | Get account rewards |
| GET | `/api/rewards/transaction/{transactionId}` | Get reward for a transaction |

## Technology Stack

| Technology | Purpose |
|---|---|
| Java 17 | Application language |
| Spring Boot 4.1.1 | Backend framework |
| Spring Data JPA | Persistence layer |
| PostgreSQL 18.6 | Relational database |
| Apache Kafka 4.3.1 | Event streaming |
| Maven | Build and dependency management |
| Docker Compose | Local infrastructure |
| SpringDoc OpenAPI | API documentation |
| JUnit 5 / Mockito | Automated testing |

## Project Structure

```text
midas-core-banking-system/
├── .mvn/
├── src/
│   ├── main/
│   │   ├── java/com/sanket/midas/
│   │   │   ├── config/
│   │   │   ├── controller/
│   │   │   ├── dto/
│   │   │   ├── entity/
│   │   │   ├── exception/
│   │   │   ├── kafka/
│   │   │   ├── repository/
│   │   │   ├── service/
│   │   │   └── transaction/
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/com/sanket/midas/
├── docker-compose.yml
├── mvnw
├── mvnw.cmd
└── pom.xml
```

## Running Locally

### Prerequisites

Install:

- Java 17
- Docker Desktop
- Git

### 1. Configure the database password

The application reads the PostgreSQL password from the `DB_PASSWORD` environment variable.

On Windows PowerShell:

```powershell
$env:DB_PASSWORD="your-password"
```

Do not commit real passwords or secret values to Git.

### 2. Start PostgreSQL and Kafka

From the project root:

```powershell
docker compose up -d
```

Check the containers:

```powershell
docker compose ps
```

PostgreSQL is exposed on host port `55432`.

Kafka is exposed on host port `9092`.

### 3. Start the application

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Linux / macOS:

```bash
./mvnw spring-boot:run
```

The API starts on:

```text
http://localhost:8080
```

## Swagger / OpenAPI

Interactive API documentation is available at:

```text
http://localhost:8080/swagger-ui/index.html
```

The generated OpenAPI specification is available at:

```text
http://localhost:8080/v3/api-docs
```

## Example Transfer

Create two accounts and then transfer money:

```json
{
  "senderAccountId": 1,
  "recipientAccountId": 2,
  "amount": 500
}
```

A completed transfer produces a transaction event and, after asynchronous processing, a reward.

For a ₹500 transfer:

```text
Sender before transfer       ₹5000
Transfer                    -₹500
Reward                        +₹5
Sender after processing      ₹4505

Recipient before transfer   ₹5000
Transfer                    +₹500
Recipient after transfer    ₹5500
```

## Testing

The project includes unit and controller tests covering account operations, transfers, validation, exception handling, incentives, and the Outbox service.

Current test suite:

```text
32 tests
0 failures
0 errors
```

Run the tests with:

Windows:

```powershell
.\mvnw.cmd clean test
```

Linux / macOS:

```bash
./mvnw clean test
```

## Design Highlights

### Transactional Outbox

Banking updates and their corresponding outbox records are written inside the same database transaction. This reduces the risk of updating the account successfully while losing the event that downstream services depend on.

### Optimistic Locking

Accounts use JPA optimistic locking so concurrent modifications can be detected and surfaced as a conflict instead of silently overwriting changes.

### Monetary Precision

Balances and transaction amounts use `BigDecimal` rather than floating-point types to avoid the precision problems associated with binary floating-point arithmetic.

### Idempotent Rewards

Each reward is tied to a unique transaction ID. The incentive processor checks for an existing reward before crediting a new one, while the database also enforces uniqueness.

## Portfolio Notes

This project demonstrates practical backend concepts including:

- REST API design
- Layered Spring Boot architecture
- Relational persistence with JPA
- Transaction management
- Optimistic locking
- Kafka producers and consumers
- Event-driven workflows
- Transactional Outbox pattern
- Idempotent downstream processing
- API validation and centralized exception handling
- Dockerized local infrastructure
- Automated testing
- OpenAPI / Swagger documentation

## License

This project is intended as a portfolio and learning project.
