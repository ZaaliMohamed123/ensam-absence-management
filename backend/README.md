# Backend Microservices

## Services
Each service runs independently on its own port with its own database.

### Architecture
- **user-service**: Port 8081, Database: user_db (PostgreSQL:5432)
- **academic-service**: Port 8082, Database: academic_db (PostgreSQL:5433)
- **absence-service**: Port 8083, Database: absence_db (PostgreSQL:5434)
- **justification-service**: Port 8084, Database: justification_db (PostgreSQL:5435)
- **notification-service**: Port 8085, Database: notification_db (PostgreSQL:5436)
- **reporting-service**: Port 8086, Database: reporting_db (PostgreSQL:5437)

## Tech Stack
- Java 21+
- Spring Boot 3.x
- PostgreSQL
- Spring Security + JWT
- Maven

## Testing
Each service can be tested independently using Postman.
