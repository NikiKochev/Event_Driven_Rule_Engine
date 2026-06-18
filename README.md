# Event-Driven Rule Engine

A production-ready, event-driven rule engine built with Spring Boot that processes messages from Kafka, applies dynamic business rules and transformations, and persists state changes to a PostgreSQL database.

## 📋 Table of Contents

- [Features](#features)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Configuration](#configuration)
- [Rule DSL](#rule-dsl)
- [Testing](#testing)
- [API Documentation](#api-documentation)
- [Monitoring](#monitoring)
- [Troubleshooting](#troubleshooting)

## ✨ Features

### Core Functionality
- **Event-Driven Processing**: Consumes messages from Kafka topics
- **Dynamic Rule Evaluation**: Supports `ALL`, `ANY`, and `NONE` condition types
- **Rich Transformation Engine**: 9 transformation operations
- **State Persistence**: Tracks message processing lifecycle in PostgreSQL
- **Error Handling**: Comprehensive exception handling and logging
- **Health Checks**: Spring Boot Actuator integration

### Supported Operations

#### Condition Operators
- `equals`, `notEquals`
- `contains`, `notContains`
- `greaterThan`, `lessThan`
- `greaterThanOrEquals`, `lessThanOrEquals`
- `exists`, `notExists`

#### Transformation Operations
- **SET**: Set or update field values
- **REMOVE**: Delete fields from JSON
- **COPY**: Copy values between fields
- **JOIN_PATHS**: Concatenate multiple field values
- **ADD**: Add numeric values
- **SUBTRACT**: Subtract numeric values
- **MULTIPLY**: Multiply numeric values
- **DIVIDE**: Divide numeric values
- **HASH**: Hash field values (SHA-256)

## 🏗️ Architecture

```

                 ┌────────────────┐
                 │ Kafka Message  │
                 └────────┬───────┘
                          │
                          ▼
     ┌─────────────────────────────────────────────────────────────┐
     │           KafkaMessageConsumer                              │
     │  @KafkaListener(topics = "${engine.kafka.input-topic}")     │
     └────────────────────┬────────────────────────────────────────┘
                          │
                          │ Deserializes
                          ▼
                 ┌─────────────────┐
                 │ IncomingMessage │
                 ├─────────────────┤
                 │ - messageId     │
                 │ - metadata:     │
                 │   * ruleType    │
                 │ - payload (JSON)│
                 └────────┬────────┘
                          │
                          ▼
  ┌───────────────────────────────────────────────────────────────┐
  │         MessageProcessingService.processMessage()             │
  │                                                               │
  │  1. Extract businessRuleType from metadata                    │
  │     String ruleType = message.getMetadata()                   │
  │                             .get("businessRuleType")          │
  │                                                               │
  │  2. Determine rule set name (e.g., "ruleset_a")               │
  │     String ruleSetName = determineRuleSetName(ruleType)       │
  └────────────────────────┬──────────────────────────────────────┘
                           │
                           ▼
  ┌─────────────────────────────────────────────────────────────┐
  │              RuleSetRegistry (In-Memory)                    │
  │                                                             │
  │  Map<String, RuleSet> registry = {                          │
  │    "ruleset_a" -> RuleSet(...),                             │
  │    "ruleset_b" -> RuleSet(...),                             │
  │    ...                                                      │
  │  }                                                          │
  │                                                             │
  │  getRuleSet(ruleSetName) → Fast O(1) lookup, no I/O        │
  └────────────────────────┬────────────────────────────────────┘
                           │
                           │ Returns RuleSet
                           ▼
  ┌──────────────────────────────────────────────────────────────┐
  │             Apply Business Logic                             │
  │                                                              │
  │  1. Validate Conditions (ALL/ANY/NONE)                       │
  │     - Extract values from payload using JsonPath             │
  │     - Apply operators (equals, greaterThan, exists, etc.)    │
  │                                                              │
  │  2. Apply Transformations                                    │
  │     - SET, REMOVE, COPY, ADD, SUBTRACT, etc.                 │
  │     - Modify payload JSON                                    │
  │                                                              │
  │  3. Save to Database                                         │
  │     - Persist MessageProcessingRecord                        │
  │                                                              │
  │  4. Send to Output Topic                                     │
  │     - KafkaMessageProducer.sendMessage()                     │
  └────────────────────────┬─────────────────────────────────────┘
                           │
                           ▼
                ┌──────────────────────┐
                │ Kafka Output Topic   │
                └──────────────────────┘
```

### Component Structure
```
src/main/java/com/nikolay/kochev/eventdrivenruleengine/
├── config/              # Configuration beans
├── engine/
│   ├── condition/       # Condition evaluation (ALL, ANY, NONE)
│   ├── transformation/  # Transformation operations
│   ├── loader/          # Rule loading from JSON
│   └── enums/           # Business enums
├── exception/           # Exception hierarchy
├── messaging/
│   ├── consumer/        # Kafka message consumer
│   └── producer/        # Kafka message producer
├── persistence/
│   ├── entity/          # JPA entities
│   ├── repository/      # Spring Data repositories
│   └── service/         # Persistence services
├── service/             # Business services
└── util/                # Utility classes
```

## 🔧 Prerequisites

- **Java**: 21 or higher
- **Maven**: 3.9 or higher
- **Docker**: 20.10 or higher (for containerized deployment)
- **Docker Compose**: 2.0 or higher

For local development without Docker:
- **Apache Kafka**: 3.x
- **PostgreSQL**: 16.x
- **Zookeeper**: 3.x

## 🚀 Quick Start

### Option 1: Docker Compose (Recommended)

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd event-driven-rule-engine
   ```

2. **Start all services**
   ```bash
   docker-compose up -d
   ```

3. **Verify services are running**
   ```bash
   docker-compose ps
   ```

4. **View application logs**
   ```bash
   docker-compose logs -f event-rule-engine
   ```

5. **Access Kafka UI** (optional)
   - Open browser: http://localhost:8080
   - View topics, messages, and consumer groups

### Option 2: Local Development

1. **Start dependencies**
   ```bash
   # Start PostgreSQL
   docker run -d -p 5433:5432 \
     -e POSTGRES_DB=event_rules_engine \
     -e POSTGRES_USER=postgres \
     -e POSTGRES_PASSWORD=postgree \
     --name postgres postgres:16-alpine

   # Start Zookeeper
   docker run -d -p 2181:2181 \
     -e ZOOKEEPER_CLIENT_PORT=2181 \
     --name zookeeper confluentinc/cp-zookeeper:7.6.0

   # Start Kafka
   docker run -d -p 9092:9092 \
     -e KAFKA_ZOOKEEPER_CONNECT=localhost:2181 \
     -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 \
     --name kafka confluentinc/cp-kafka:7.6.0
   ```

2. **Build the application**
   ```bash
   mvn clean install
   ```

3. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

## ⚙️ Configuration

### Application Properties

Key configuration options in `application.properties`:

```properties
# Kafka Configuration
engine.kafka.input-topic=engine.events.input
engine.kafka.output-topic=engine.events.output
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=event-rules-engine

# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5433/event_rules_engine
spring.datasource.username=postgres
spring.datasource.password=postgree

# Rules Configuration
engine.rules.file=classpath:rules.json

# JPA Configuration
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true

# Flyway Configuration
spring.flyway.enabled=true
```

### Environment Variables

Override configuration using environment variables:

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/event_rules_engine
export SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:29092
export ENGINE_KAFKA_INPUT_TOPIC=engine.events.input
export ENGINE_KAFKA_OUTPUT_TOPIC=engine.events.output
```

## 📖 Rule DSL

### Rule Structure

Rules are defined in JSON format (`src/main/resources/rules.json`):

```json
{
  "ruleSets": [
    {
      "businessRuleType": "user_validation",
      "conditionType": "ALL",
      "conditions": [
        {
          "path": "$.user.age",
          "operator": "greaterThan",
          "value": "18"
        },
        {
          "path": "$.user.email",
          "operator": "exists"
        }
      ],
      "transformations": [
        {
          "path": "$.status",
          "operator": "SET",
          "value": "verified"
        },
        {
          "path": "$.user.age",
          "operator": "ADD",
          "value": 1
        }
      ]
    }
  ]
}
```

### Condition Types

- **ALL**: All conditions must be true
- **ANY**: At least one condition must be true
- **NONE**: No conditions should be true (skip validation)

### JsonPath Syntax

Use JsonPath to navigate nested JSON:
- `$.field` - Root level field
- `$.user.name` - Nested field
- `$.items[0].price` - Array element

### Example Input Message

```json
{
  "messageId": "msg-123",
  "timestamp": "2026-06-18T10:00:00Z",
  "payload": {
    "user": {
      "name": "John Doe",
      "age": 25,
      "email": "john@example.com"
    }
  },
  "metadata": {
    "businessRuleType": "user_validation"
  }
}
```

### Example Output Message

```json
{
  "messageId": "msg-123",
  "timestamp": "2026-06-18T10:00:00Z",
  "payload": {
    "user": {
      "name": "John Doe",
      "age": 26,
      "email": "john@example.com"
    },
    "status": "verified"
  },
  "metadata": {
    "businessRuleType": "user_validation"
  }
}
```

## 🧪 Testing

### Run All Tests

```bash
mvn test
```

### Run Specific Test Class

```bash
mvn test -Dtest=AllConditionTest
```

### Test Coverage

The project includes **18 test classes** covering:
- Condition evaluation (ALL, ANY)
- All 9 transformation operations
- Exception handling
- Persistence layer


## 📊 API Documentation

### Database Schema

View message processing records:

```sql
SELECT * FROM message_processing_records ORDER BY created_at DESC;
```

Columns:
- `id`: UUID primary key
- `message_id`: Original message ID
- `message_type`: Business rule type
- `status`: Processing status (RECEIVED, VALIDATED, COMPLETED, etc.)
- `error_message`: Error details if failed
- `created_at`: Record creation time
- `completed_at`: Processing completion time

## 📈 Monitoring

### View Logs

```bash
# Application logs
docker-compose logs -f event-rule-engine

# Kafka logs
docker-compose logs -f kafka

# Database logs
docker-compose logs -f postgres
```

### Kafka UI

Access Kafka UI at http://localhost:8080 to:
- View topics and messages
- Monitor consumer groups
- Check consumer lag
- Browse message content

### Database Monitoring

```bash
# Connect to PostgreSQL
docker exec -it event-rule-engine-postgres psql -U postgres -d event_rules_engine

# View processing statistics
SELECT status, COUNT(*) FROM message_processing_records GROUP BY status;
```


## 📚 Additional Documentation

- **[HOW_RULE_EVALUATION_WORKS.md](HOW_RULE_EVALUATION_WORKS.md)** - Detailed rule evaluation guide
- **[SAMPLE_MESSAGES.md](SAMPLE_MESSAGES.md)** - Sample test messages
- **[REQUIREMENTS_CHECKLIST.md](REQUIREMENTS_CHECKLIST.md)** - Requirements compliance



## 👤 Author

**Nikolay Kochev**
- Email: nikolaykochev@gmail.com
- GitHub: [@nikolay-kochev](https://github.com/nikolay-kochev)


