# Employee Service

A Spring Boot 2.7.x REST API built with Java 17 for managing Employee records. The service provides full CRUD operations, role-based security (ADMIN/USER), pagination, search, soft-delete, and comprehensive testing with Testcontainers against a real MySQL database.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [Testing](#testing)
- [API Documentation](#api-documentation)
- [Docker Usage](#docker-usage)
- [Available Credentials](#available-credentials)
- [Quick-Start Examples](#quick-start-examples)
- [Project Structure](#project-structure)
- [Architecture & Design Decisions](#architecture--design-decisions)
- [Proof of Functionality](#proof-of-functionality)
- [Postman Collection](#postman-collection)

---

## Prerequisites

| Tool             | Version   | Notes                                      |
|------------------|-----------|--------------------------------------------|
| Java (JDK)       | 17        | LTS release required                       |
| Maven            | 3.8+      | Wrapper included (`mvnw` / `mvnw.cmd`)     |
| Docker           | 20.10+    | Required for Testcontainers and deployment  |
| Docker Desktop   | ≤ 4.55.0  | Versions above 4.55.0 have a known bug that breaks Testcontainers connectivity |
| Docker Compose   | 2.0+      | Required for local development environment  |

> **⚠️ Docker Desktop compatibility:** Docker Desktop versions newer than 4.55.0 introduce a named-pipe proxy bug that returns empty `/info` responses to the Java Docker client, causing Testcontainers to fail with `Communications link failure`. If you experience this, downgrade to [Docker Desktop 4.55.0](https://docs.docker.com/desktop/release-notes/#4550) or earlier.

## Installation

1. **Clone the repository:**

   ```bash
   git clone <repository-url>
   cd employees
   ```

2. **Build the project:**

   ```bash
   # Linux / macOS
   ./mvnw clean package -DskipTests

   # Windows
   .\mvnw.cmd clean package -DskipTests
   ```

## Configuration

### Application Properties

The main configuration file is `src/main/resources/application.yml`. Key configurable parameters:

| Parameter                        | Default                    | Description                              |
|----------------------------------|----------------------------|------------------------------------------|
| `server.port`                    | `8080`                     | HTTP server port                         |
| `employee.minimum-age`           | `18`                       | Minimum age for employee registration    |
| `employee.search.min-length`     | `3`                        | Minimum characters for name search       |
| `employee.name.validation-regex` | `^[\p{L} '\-]+$`          | Regex pattern for name field validation  |
| `spring.data.web.pageable.default-page-size` | `20`          | Default page size for paginated results  |
| `spring.data.web.pageable.max-page-size`     | `100`         | Maximum allowed page size                |

### Spring Profiles

| Profile | Activation                                          | Database                        | Purpose                          |
|---------|-----------------------------------------------------|---------------------------------|----------------------------------|
| `dev`   | `--spring.profiles.active=dev`                      | MySQL via docker-compose        | Local development with debug logging |
| `test`  | Activated automatically by `@ActiveProfiles("test")`| MySQL via Testcontainers        | Automated testing with real MySQL    |
| `prod`  | `--spring.profiles.active=prod`                     | MySQL via environment variables | Production-like deployment           |

Profile-specific configuration files:
- `application-dev.yml` — MySQL on `localhost:3306`, DEBUG logging
- `application-test.yml` — Testcontainers-managed MySQL, `create-drop` DDL
- `application-prod.yml` — MySQL via `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD` environment variables

## Running the Application

### With Maven (requires a running MySQL instance)

Start MySQL first using Docker Compose, then run the application:

```bash
# Start MySQL only
docker-compose up -d mysql

# Run the application with the dev profile
# Linux / macOS
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Windows
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

### With Docker Compose (recommended for local development)

```bash
# Start both MySQL and the application
docker-compose up --build
```

The application will be available at `http://localhost:8080`.

## Testing

### Unit Tests

Unit tests use JUnit 5 and Mockito to test the service and controller layers in isolation. No Docker required.

```bash
# Linux / macOS
./mvnw test

# Windows
.\mvnw.cmd test
```

### Integration Tests

Integration tests use Testcontainers to spin up a real MySQL instance in Docker. **Docker must be running** before executing integration tests.

```bash
# Linux / macOS
./mvnw test -Dinclude.integration=true

# Windows
.\mvnw.cmd test -Dinclude.integration=true
```

> **Note:** Integration tests are excluded from the default `mvn test` run. Use the flag above to include them. Testcontainers will automatically pull the MySQL Docker image on first run.

## API Documentation

Interactive Swagger UI documentation is available when the application is running:

- **Swagger UI:** [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **OpenAPI JSON:** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

## Docker Usage

### Local Development

```bash
# Start all services (MySQL + App)
docker-compose up --build

# Start in detached mode
docker-compose up --build -d

# Stop all services
docker-compose down

# Stop and remove volumes (clean database)
docker-compose down -v
```

### Production-like Deployment

```bash
# Start with production configuration
docker-compose -f docker-compose.prod.yml up --build -d

# Stop production services
docker-compose -f docker-compose.prod.yml down
```

You can override production defaults with environment variables:

```bash
DB_PASSWORD=mysecurepassword DB_NAME=my_employees docker-compose -f docker-compose.prod.yml up --build -d
```

### Build Docker Image Only

```bash
docker build -t employee-service:latest .
```

## Available Credentials

The application uses HTTP Basic Authentication with two in-memory users:

| Username | Password   | Role    | Permissions                                |
|----------|------------|---------|--------------------------------------------|
| `admin`  | `admin123` | ADMIN   | Full access: GET, POST, PUT, PATCH, DELETE |
| `user`   | `user123`  | USER    | Read-only access: GET endpoints only       |

## Quick-Start Examples

### Create an Employee

```bash
curl -s -X POST http://localhost:8080/api/v1/employees \
  -H "Content-Type: application/json" \
  -u admin:admin123 \
  -d '[{
    "firstName": "María",
    "middleName": "José",
    "fatherName": "García",
    "motherName": "López",
    "dateOfBirth": "15-03-1990",
    "gender": "FEMALE",
    "position": "Software Engineer"
  }]'
```

### Get All Employees (Paginated)

```bash
curl -s http://localhost:8080/api/v1/employees?page=0&size=10 \
  -u admin:admin123
```

### Soft-Delete an Employee

```bash
curl -s -X DELETE http://localhost:8080/api/v1/employees/<EMPLOYEE_UUID> \
  -u admin:admin123
```

For the full set of endpoint examples, see the [Postman Collection](#postman-collection) section below.

## Project Structure

```
├── Dockerfile                          # Multi-stage Docker build
├── docker-compose.yml                  # Local development (MySQL + App)
├── docker-compose.prod.yml             # Production-like deployment
├── pom.xml                             # Maven project configuration
├── README.md                           # This file
├── documents/
│   └──  ExamDescription.md              # Original exam requirements
├── docs/
│   ├── postman/                        # Postman collection & environment
│   └── screenshots/                    # Proof of functionality screenshots
├── .github/
│   └── workflows/
│       ├── ci.yml                      # GitHub Actions CI/CD pipeline
│       └── cd.yml                      # GitHub Actions CI/CD pipeline
└── src/
    ├── main/
    │   ├── java/com/demo/employees/
    │   │   ├── EmployeesApplication.java   # Main application class
    │   │   ├── common/                     # Constants, utility classes
    │   │   ├── config/                     # Security, Swagger, WebMvc, Interceptor
    │   │   ├── controller/                 # REST controllers
    │   │   ├── dto/
    │   │   │   ├── request/                # EmployeeRequest, EmployeePatchRequest
    │   │   │   └── response/               # ApiResponse, EmployeeResponse, PaginatedResponse
    │   │   ├── entity/                     # Employee JPA entity
    │   │   ├── enums/                      # Gender enum
    │   │   ├── exception/                  # Custom exceptions, GlobalExceptionHandler
    │   │   ├── mapper/                     # MapStruct EmployeeMapper
    │   │   ├── repository/                 # Spring Data JPA repositories
    │   │   ├── service/                    # Service interfaces
    │   │   │   └── impl/                   # Service implementations
    │   │   └── validation/                 # Custom validators (@ValidDateOfBirth)
    │   └── resources/
    │       ├── application.yml             # Shared configuration
    │       ├── application-dev.yml         # Development profile
    │       ├── application-test.yml        # Test profile (Testcontainers)
    │       └── application-prod.yml        # Production profile
    └── test/
        └── java/com/demo/employees/
            ├── controller/                 # Controller unit tests (MockMvc)
            ├── exception/                  # Exception handler unit tests
            ├── integration/                # Integration tests (Testcontainers)
            ├── service/impl/               # Service unit tests (Mockito)
            └── validation/                 # Validator unit tests
```

## Architecture & Design Decisions

The application follows a classic **layered architecture** within a single deployable Spring Boot JAR:

1. **Controller Layer** — Handles HTTP requests, triggers Bean Validation, delegates to the service layer
2. **Service Layer** — Contains business logic (batch creation, soft-delete, search, pagination)
3. **Mapper Layer** — MapStruct-based type-safe mapping between entities and DTOs
4. **Repository Layer** — Spring Data JPA for database operations against MySQL

Cross-cutting concerns are handled by:
- **Spring Security** — HTTP Basic Auth with role-based access control
- **Global Exception Handler** — Unified `ApiResp` error formatting via `@ControllerAdvice`
- **Request Logging Interceptor** — Logs HTTP method, URI, and headers (excluding Authorization)
- **Bean Validation** — Input validation with custom annotations (`@ValidDOB`)

Key design decisions include:
- **UUID** identifiers for security and microservices readiness
- **Soft-delete** strategy preserving records for audit purposes
- **Age calculated dynamically** from date of birth (never stored)
- **Testcontainers** with real MySQL instead of H2 for test fidelity
- **Configurable parameters** in `application.yml` (minimum age, search length, name regex)


## Proof of Functionality

Screenshots demonstrating the application's functionality are available in the [`docs/screenshots/`](docs/screenshots/) directory. These include evidence of:

- CRUD operations (create, read, update, delete)
- Search and pagination
- Role-based security (ADMIN vs USER access)
- Validation error handling
- Health check and version endpoints
- Swagger UI documentation

## Postman Collection

A ready-to-use Postman collection is provided in the [`docs/postman/`](docs/postman/) directory for testing all API endpoints.

### Files

| File | Description |
|------|-------------|
| `Employee-Service.postman_collection.json` | Complete API collection organized by functional area |
| `Employee-Service.postman_environment.json` | Environment variables (base URL, credentials) |

### Import Instructions

1. Open **Postman**
2. Click **Import** (top-left)
3. Drag and drop both files from `docs/postman/`, or click **Upload Files** and select them
4. Select the **"Employee Service - Local"** environment from the environment dropdown (top-right)
5. Replace `<EMPLOYEE_UUID>` placeholders with actual UUIDs from POST/GET responses

