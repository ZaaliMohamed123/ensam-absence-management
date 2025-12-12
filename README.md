# ENSAM Absence Management System

Web application for managing student absences at École Nationale Supérieure d'Arts et Métiers (ENSAM).

## 📋 Project Overview

This system allows:
- **Administrators**: Full control over classes, schedules, courses, students, and absence validation
- **Teachers**: Mark absences for their assigned courses and view statistics
- **Students**: View their absences, submit justifications, and track their status

## 🏗️ Architecture

Microservices architecture with standalone services communicating via REST APIs.

### Services

| Service | Port | Database | Description |
|---------|------|----------|-------------|
| **user-service** | 8081 | user_db (PostgreSQL:5432) | Authentication & user management |
| **academic-service** | 8082 | academic_db (PostgreSQL:5433) | Classes, courses, schedules (Admin only) |
| **absence-service** | 8083 | absence_db (PostgreSQL:5434) | Absence recording & tracking |
| **justification-service** | 8084 | justification_db (PostgreSQL:5435) | Justification management |
| **notification-service** | 8085 | notification_db (PostgreSQL:5436) | Email notifications |
| **reporting-service** | 8086 | reporting_db (PostgreSQL:5437) | Statistics & reports generation |

## 🛠️ Tech Stack

- **Language**: Java 21
- **Framework**: Spring Boot 4.0.0
- **Security**: Spring Security + JWT
- **Database**: PostgreSQL 15
- **ORM**: Spring Data JPA / Hibernate
- **Build Tool**: Maven
- **Containerization**: Docker & Docker Compose
- **API Testing**: Postman

## 📦 Project Structure

```

ensam-absence-management/
├── user-service/
│   ├── src/main/java/com/ensam/userservice/
│   │   ├── entities/
│   │   ├── repositories/
│   │   ├── services/
│   │   ├── controllers/
│   │   ├── dto/
│   │   ├── security/
│   │   ├── config/
│   │   └── exception/
│   └── pom.xml
├── academic-service/
├── absence-service/
├── justification-service/
├── notification-service/
├── reporting-service/
├── docker-compose.yml
├── .gitignore
└── README.md

```

## 🚀 Getting Started

### Prerequisites

- Java 21 or higher
- Maven 3.8+
- Docker & Docker Compose
- Postman (for API testing)
- Git

### Installation

1. **Clone the repository**
```

git clone https://github.com/YOUR_USERNAME/ensam-absence-management.git
cd ensam-absence-management

```

2. **Start all databases**
```

docker-compose up -d

```

3. **Verify databases are running**
```

docker-compose ps

```

4. **Build all services**
```


# For each service (example with user-service)

cd user-service
./mvnw clean install
cd ..

```

5. **Run a service**
```

cd user-service
./mvnw spring-boot:run

```

Or run in background:
```

./mvnw spring-boot:run \&

```

## 🔧 Database Management

### Start databases
```

docker-compose up -d

```

### Stop databases
```

docker-compose down

```

### Stop and remove all data
```

docker-compose down -v

```

### View logs
```

docker-compose logs -f

```

### Access PostgreSQL CLI
```


# Example for user_db

docker exec -it user-db psql -U postgres -d user_db

```

## 👥 User Roles & Permissions

### 🔴 Administrator (ROLE_ADMIN)
- Manage classes, courses, and schedules (full CRUD)
- View all students and teachers
- Validate/reject absence justifications
- Access all reports and statistics
- Manage user accounts

### 🟢 Teacher (ROLE_TEACHER)
- View assigned courses (read-only)
- Mark absences for their courses only
- View absences for their courses
- View justification status
- Access course-specific reports

### 🔵 Student (ROLE_STUDENT)
- View personal schedule
- View personal absences
- Submit absence justifications
- Track justification status
- View personal statistics

## 📡 API Endpoints

### User Service (Port 8081)

**Authentication**
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login

**User Management**
- `GET /api/users/profile` - Get current user profile
- `PUT /api/users/profile` - Update profile
- `GET /api/users/students` - Get all students (Admin only)
- `GET /api/users/teachers` - Get all teachers (Admin only)
- `GET /api/users/{userId}` - Get user by ID (Admin only)

### Academic Service (Port 8082)

**Classes**
- `POST /api/classes` - Create class (Admin only)
- `GET /api/classes` - Get all classes
- `GET /api/classes/{id}` - Get class by ID
- `PUT /api/classes/{id}` - Update class (Admin only)
- `DELETE /api/classes/{id}` - Delete class (Admin only)

**Courses**
- `POST /api/courses` - Create course (Admin only)
- `GET /api/courses` - Get all courses
- `GET /api/courses/teacher/{teacherId}` - Get teacher's courses
- `GET /api/courses/class/{classId}` - Get class courses
- `PUT /api/courses/{id}` - Update course (Admin only)

**Schedules**
- `POST /api/schedules` - Create schedule (Admin only)
- `GET /api/schedules/course/{courseId}` - Get course schedules
- `GET /api/schedules/class/{classId}` - Get class schedules

### Absence Service (Port 8083)

- `POST /api/absences` - Mark absence (Teacher only)
- `GET /api/absences/course/{courseId}` - Get course absences (Teacher/Admin)
- `GET /api/absences/student/{studentId}` - Get student absences (Student/Admin)
- `PUT /api/absences/{id}` - Update absence (Admin only)
- `DELETE /api/absences/{id}` - Delete absence (Admin only)

### Justification Service (Port 8084)

- `POST /api/justifications` - Submit justification (Student only)
- `GET /api/justifications/student/{studentId}` - Get student justifications
- `GET /api/justifications/pending` - Get pending justifications (Admin only)
- `PUT /api/justifications/{id}/approve` - Approve justification (Admin only)
- `PUT /api/justifications/{id}/reject` - Reject justification (Admin only)

### Notification Service (Port 8085)

- `POST /api/notifications/send` - Send notification
- `GET /api/notifications/user/{userId}` - Get user notifications

### Reporting Service (Port 8086)

- `GET /api/reports/student/{studentId}` - Student report (Student/Admin)
- `GET /api/reports/course/{courseId}` - Course report (Teacher/Admin)
- `GET /api/reports/class/{classId}` - Class report (Admin only)
- `GET /api/reports/statistics` - Global statistics (Admin only)

## 🧪 Testing with Postman

1. Import the Postman collection (to be provided)
2. Set environment variables:
   - `BASE_URL_USER`: http://localhost:8081
   - `BASE_URL_ACADEMIC`: http://localhost:8082
   - `BASE_URL_ABSENCE`: http://localhost:8083
   - etc.

3. Test authentication flow:
   - Register users (Admin, Teacher, Student)
   - Login and save tokens
   - Test role-based access control

## 🔐 Security

- JWT-based authentication
- Role-based access control (RBAC)
- Password encryption with BCrypt
- Stateless session management
- CORS configured for frontend integration

## 📊 Development Status

- [x] Project structure setup
- [x] Database configuration
- [x] User service implementation
- [ ] Academic service implementation
- [ ] Absence service implementation
- [ ] Justification service implementation
- [ ] Notification service implementation
- [ ] Reporting service implementation
- [ ] API Gateway integration
- [ ] Service discovery with Eureka
- [ ] Centralized configuration
- [ ] Complete API documentation
- [ ] Unit & integration tests

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 License

This project is developed as part of an academic project at ENSAM.

## 👨‍💻 Authors

- ENSAM Students - Initial work

## 📧 Contact

For questions or support, please contact the development team.

---

**ENSAM - École Nationale Supérieure d'Arts et Métiers**  
Mini-projet: Big Data Analytics, Systèmes Distribués, DevOps/MLOps  
Academic Year: 2025-2026
EOF
```



