# E-Commerce Microservices with JWT

A full production-grade Spring Boot microservices e-commerce platform.

## Architecture

```
Client -> API Gateway (8080) -> Microservices
                              |-- Auth Service    (8081) [PostgreSQL, Redis]
                              |-- User Service    (8082) [PostgreSQL]
                              |-- Product Service (8083) [PostgreSQL, Kafka]
                              |-- Order Service   (8084) [PostgreSQL, Kafka]
                              |-- Payment Service (8085) [PostgreSQL, Kafka]
                              |-- Notification    (8086) [PostgreSQL, Kafka, SMTP]

Infrastructure:
  Service Discovery (Eureka)  (8761)
  Config Server               (8888)
  PostgreSQL                  (5432)
  Redis                       (6379)
  Kafka + Zookeeper           (9092)
```

## Features

- **JWT Authentication** — Access + Refresh token flow
- **API Gateway** — JWT filter, load balancing via Eureka
- **Service Discovery** — Eureka with basic auth
- **Config Server** — Centralized configuration
- **Event-Driven** — Kafka for order/notification events
- **Redis** — Token caching, rate limiting
- **Pagination** — All list endpoints paginated

## Quick Start

### With Docker Compose
```bash
docker-compose up -d
```

### Without Docker (run in order)
```bash
# 1. Start infrastructure: PostgreSQL, Redis, Kafka
# 2. Start Config Server
cd config-server && mvn spring-boot:run

# 3. Start Eureka
cd service-discovery && mvn spring-boot:run

# 4. Start all services (each in separate terminal)
cd auth-service && mvn spring-boot:run
cd user-service && mvn spring-boot:run
cd product-service && mvn spring-boot:run
cd order-service && mvn spring-boot:run
cd payment-service && mvn spring-boot:run
cd notification-service && mvn spring-boot:run

# 5. Start API Gateway last
cd api-gateway && mvn spring-boot:run
```

## API Endpoints (all via Gateway on port 8080)

### Auth
| Method | URL | Auth | Description |
|--------|-----|------|-------------|
| POST | /api/auth/register | No | Register user |
| POST | /api/auth/login | No | Login, get tokens |
| POST | /api/auth/refresh | No | Refresh access token |
| POST | /api/auth/logout | Yes | Logout |

### Products
| Method | URL | Auth | Description |
|--------|-----|------|-------------|
| GET | /api/products | No | List products |
| GET | /api/products/{id} | No | Get product |
| GET | /api/products/search | No | Search products |
| POST | /api/products | Yes (SELLER) | Create product |
| PUT | /api/products/{id} | Yes (SELLER) | Update product |
| DELETE | /api/products/{id} | Yes (SELLER) | Delete product |

### Orders
| Method | URL | Auth | Description |
|--------|-----|------|-------------|
| POST | /api/orders | Yes | Place order |
| GET | /api/orders | Yes | My orders |
| GET | /api/orders/{orderNumber} | Yes | Get order |
| PUT | /api/orders/{orderNumber}/cancel | Yes | Cancel order |

### Payments
| Method | URL | Auth | Description |
|--------|-----|------|-------------|
| POST | /api/payments | Yes | Process payment |
| GET | /api/payments/my | Yes | My payments |
| GET | /api/payments/{paymentId} | Yes | Get payment |
| POST | /api/payments/{paymentId}/refund | Yes | Refund payment |

### User Profile
| Method | URL | Auth | Description |
|--------|-----|------|-------------|
| GET | /api/users/profile | Yes | Get profile |
| PUT | /api/users/profile | Yes | Update profile |
| POST | /api/users/profile/addresses | Yes | Add address |

## Example Usage

```bash
# 1. Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"user@test.com","password":"password123","firstName":"John","lastName":"Doe"}'

# 2. Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@test.com","password":"password123"}'

# 3. Use token
curl http://localhost:8080/api/products \
  -H "Authorization: Bearer <your_access_token>"
```

## Tech Stack
- **Java 17** + **Spring Boot 3.2**
- **Spring Cloud 2023** (Gateway, Eureka, Config, OpenFeign)
- **Spring Security** + **JWT (jjwt 0.11.5)**
- **Spring Kafka** for async messaging
- **PostgreSQL** per service (database-per-service pattern)
- **Redis** for caching & token blacklisting
- **Docker** + **Docker Compose**
- **Lombok**, **MapStruct**, **Bean Validation**
