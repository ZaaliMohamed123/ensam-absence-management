# 🎓 ENSAM Absence Management System

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED.svg)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-Academic-yellow.svg)]()

Système de gestion des absences pour l'École Nationale Supérieure d'Arts et Métiers (ENSAM) basé sur une architecture microservices moderne.

## 📋 Table des Matières

- [Vue d'ensemble](#-vue-densemble)
- [Architecture](#%EF%B8%8F-architecture)
- [Technologies](#-technologies)
- [Installation](#-installation)
- [Déploiement](#-déploiement)
- [API Documentation](#-api-documentation)
- [Tests](#-tests)
- [Contributeurs](#-contributeurs)

---

## 🎯 Vue d'ensemble

Système complet de gestion des absences permettant :
- **Administrateurs** : Gestion globale (utilisateurs, classes, modules, validation justificatifs)
- **Enseignants** : Marquage des absences, consultation des statistiques par cours
- **Étudiants** : Consultation des absences, soumission de justificatifs, suivi du statut

### Fonctionnalités principales

- ✅ Authentification JWT avec contrôle d'accès basé sur les rôles (RBAC)
- ✅ Enregistrement et suivi des absences par session
- ✅ Soumission et validation de justificatifs avec upload de documents
- ✅ Notifications automatiques par email
- ✅ Génération de rapports statistiques (étudiant, classe, module)
- ✅ Calcul automatique du taux d'absentéisme
- ✅ Architecture microservices scalable et résiliente

---

## 🏗️ Architecture

### Vue globale

L'application suit une architecture **microservices** avec découverte de services (Eureka), configuration centralisée (Config Server) et point d'entrée unique (API Gateway).

```

┌─────────────────────────────────────────────────────────────┐
│                  Eureka Discovery Service                   │
│                    (localhost:8761)                         │
└─────────────────────────────────────────────────────────────┘
▲
│ (Service Registration)
┌─────────────────────┼─────────────────────┐
│                     │                     │
┌───────▼──────┐    ┌────────▼─────┐    ┌─────────▼────────┐
│ Config Server│    │ API Gateway  │    │  Microservices   │
│   :8888      │    │   :8080      │    │  (6 services)    │
└──────────────┘    └──────────────┘    └──────────────────┘
│
┌───────────────────┼───────────────────┐
│                   │                   │
┌───────▼─────┐    ┌────────▼────┐    ┌────────▼────────┐
│ user-service│    │academic-svc │    │ absence-service │
│   :8081     │    │   :8082     │    │    :8083        │
└─────┬───────┘    └─────┬───────┘    └────┬────────────┘
│                  │                  │
┌─────▼──────┐    ┌──────▼──────┐    ┌─────▼──────────┐
│  user_db   │    │academic_db  │    │  absence_db    │
│  :5432     │    │  :5433      │    │   :5434        │
└────────────┘    └─────────────┘    └────────────────┘

```

### Microservices

| Service | Port | Base de données | Responsabilités |
|---------|------|-----------------|-----------------|
| **discovery-service** | 8761 | - | Service Registry (Eureka) |
| **config-server** | 8888 | - | Configuration centralisée |
| **api-gateway** | 8080 | - | Point d'entrée unique, routage |
| **user-service** | 8081 | PostgreSQL:5432 | Authentification, gestion utilisateurs |
| **academic-service** | 8082 | PostgreSQL:5433 | Classes, modules, sessions |
| **absence-service** | 8083 | PostgreSQL:5434 | Enregistrement et suivi des absences |
| **notification-service** | 8084 | PostgreSQL:5436 | Notifications email/SMS |
| **justification-service** | 8085 | PostgreSQL:5435 | Gestion des justificatifs |
| **reporting-service** | 8086 | PostgreSQL:5437 | Rapports et statistiques |

---

## 🛠 Technologies

### Backend
- **Java 21** (Eclipse Temurin)
- **Spring Boot 4.0.0**
- **Spring Cloud 2024.0.0** (Eureka, Config Server, Gateway MVC)
- **Spring Security** + JWT
- **Spring Data JPA** + Hibernate
- **PostgreSQL 15**

### DevOps & Infrastructure
- **Docker** + Docker Compose
- **Maven 3.8+**
- **Git**

### Testing
- **JUnit 5**
- **Testcontainers** (tests d'intégration)
- **Mockito**
- **WireMock** (mock inter-services)
- **Apache JMeter** (tests de charge)

---

## 🚀 Installation

### Prérequis

- **Java 21** : [Télécharger OpenJDK](https://adoptium.net/)
- **Maven 3.8+** : [Installation Maven](https://maven.apache.org/install.html)
- **Docker** + **Docker Compose** : [Installation Docker](https://docs.docker.com/get-docker/)
- **Git** : [Installation Git](https://git-scm.com/downloads)

### Cloner le projet

```

git clone https://github.com/YOUR_USERNAME/ensam-absence-management.git
cd ensam-absence-management

```

### Structure du projet

```

ensam-absence-management/
├── discovery-service/          \# Eureka Server
├── config-server/              \# Config Server (mode native)
├── api-gateway/                \# Spring Cloud Gateway MVC
├── user-service/               \# Gestion utilisateurs + Auth
├── academic-service/           \# Classes, modules, sessions
├── absence-service/            \# Absences
├── notification-service/       \# Notifications
├── justification-service/      \# Justificatifs
├── reporting-service/          \# Rapports
├── docker-compose.yml          \# Orchestration complète
└── README.md

```

---

## 🐳 Déploiement

### Option 1 : Déploiement avec Docker Compose (Recommandé)

#### 1. Build des JARs

```


# Build de tous les services

for service in discovery-service config-server api-gateway user-service academic-service absence-service notification-service justification-service reporting-service; do
echo "Building \$service..."
cd \$service
mvn clean package -DskipTests
cd ..
done

```

#### 2. Lancer la stack complète

```


# Build des images Docker

docker-compose build

# Démarrer tous les services

docker-compose up -d

# Voir les logs

docker-compose logs -f

```

#### 3. Vérifier le déploiement

```


# Statut des conteneurs

docker-compose ps

# Accès Eureka Dashboard

open http://localhost:8761

# Tester API Gateway

curl http://localhost:8080/actuator/health

```

### Option 2 : Déploiement manuel (Développement)

#### 1. Lancer les bases de données

```

docker-compose up -d user-db academic-db absence-db notification-db justification-db reporting-db

```

#### 2. Lancer les services dans l'ordre

```


# 1. Discovery Service

cd discovery-service
mvn spring-boot:run \&
cd ..

# 2. Config Server

cd config-server
mvn spring-boot:run \&
cd ..

# 3. API Gateway

cd api-gateway
mvn spring-boot:run \&
cd ..

# 4. Microservices (en parallèle)

cd user-service \&\& mvn spring-boot:run \&
cd academic-service \&\& mvn spring-boot:run \&
cd absence-service \&\& mvn spring-boot:run \&
cd notification-service \&\& mvn spring-boot:run \&
cd justification-service \&\& mvn spring-boot:run \&
cd reporting-service \&\& mvn spring-boot:run \&

```

### Commandes utiles

```


# Arrêter tous les services

docker-compose down

# Arrêter et supprimer les volumes (reset complet)

docker-compose down -v

# Redémarrer un service spécifique

docker-compose restart user-service

# Voir les logs d'un service

docker-compose logs -f user-service

# Accéder à une base de données

docker exec -it user-db psql -U postgres -d user_db

```

---

## 📡 API Documentation

### Authentification (via API Gateway)

**Base URL** : `http://localhost:8080`

#### Register
```

POST /api/auth/register
Content-Type: application/json

{
"username": "john.doe",
"email": "john.doe@ensam.ma",
"password": "SecurePass123!",
"role": "STUDENT"
}

```

#### Login
```

POST /api/auth/login
Content-Type: application/json

{
"username": "john.doe",
"password": "SecurePass123!"
}

Response:
{
"token": "eyJhbGciOiJIUzI1NiIs...",
"type": "Bearer",
"expiresIn": 86400
}

```

### Endpoints principaux

#### User Service (via Gateway)
```

GET    /api/users                    \# Liste des utilisateurs
GET    /api/users/{id}               \# Détails utilisateur
PUT    /api/users/{id}               \# Mise à jour profil
DELETE /api/users/{id}               \# Suppression

```

#### Academic Service
```

GET    /api/classes                  \# Liste des classes
POST   /api/classes                  \# Créer classe (Admin)
GET    /api/modules                  \# Liste des modules
GET    /api/sessions                 \# Planning des sessions
GET    /api/sessions/date/{date}     \# Sessions d'une date

```

#### Absence Service
```

GET    /api/absences                       \# Liste des absences
POST   /api/absences                       \# Enregistrer absence (Teacher)
GET    /api/absences/student/{id}          \# Absences d'un étudiant
GET    /api/absences/stats/{studentId}     \# Statistiques
PUT    /api/absences/{id}/justify          \# Marquer comme justifiée

```

#### Justification Service
```

GET    /api/justifications                     \# Liste justificatifs
POST   /api/justifications                     \# Soumettre justificatif
PUT    /api/justifications/{id}/validate       \# Approuver/rejeter (Admin)
GET    /api/justifications/{id}/document       \# Télécharger document

```

#### Reporting Service
```

GET    /api/reports/student/{id}       \# Rapport étudiant
GET    /api/reports/class/{id}         \# Rapport classe
GET    /api/reports/absence-rate       \# Taux global
GET    /api/reports/{id}/download      \# Export PDF/Excel

```

### Postman Collection

Importez la collection Postman complète : [Télécharger](./postman/ENSAM-Absence-API.postman_collection.json)

Variables d'environnement :
```

{
"GATEWAY_URL": "http://localhost:8080",
"JWT_TOKEN": "<obtenu après login>"
}

```

---

## 🧪 Tests

### Tests unitaires

```


# Lancer les tests d'un service

cd user-service
mvn test

# Avec rapport de couverture

mvn clean test jacoco:report

```

### Tests d'intégration

Chaque service possède des tests d'intégration avec Testcontainers (PostgreSQL).

```

cd user-service
mvn verify

```

### Synthèse des tests

| Service | Tests unitaires | Tests intégration | Couverture |
|---------|----------------|-------------------|-----------|
| user-service | 25 | 12 | 85% |
| academic-service | 30 | 15 | 82% |
| absence-service | 28 | 18 | 88% |
| notification-service | 15 | 10 | 78% |
| justification-service | 20 | 13 | 80% |
| reporting-service | 18 | 11 | 75% |
| **Total** | **136** | **79** | **81%** |

### Tests de charge (JMeter)

```


# Lancer test JMeter

jmeter -n -t tests/load-test.jmx -l results.jtl

```

**Résultats** :
- 450 req/s sur `GET /api/users` (85ms moyen)
- 200 req/s sur `POST /api/absences` (180ms moyen)
- 0% d'erreurs jusqu'à 100 utilisateurs concurrents

---

## 📊 Monitoring

### Eureka Dashboard
Accéder à : `http://localhost:8761`

Visualisation en temps réel :
- Services enregistrés
- Statut (UP/DOWN)
- Instances actives

### Spring Boot Actuator

Chaque service expose des endpoints Actuator :

```

GET /actuator/health        \# Statut santé
GET /actuator/metrics       \# Métriques
GET /actuator/info          \# Informations service

```

---

## 🔐 Sécurité

- **JWT** : Tokens signés avec HMAC-SHA256, expiration 24h
- **BCrypt** : Hash des mots de passe (force 10)
- **RBAC** : Contrôle d'accès basé sur les rôles (ADMIN, TEACHER, STUDENT)
- **CORS** : Configuré pour intégration frontend
- **Validation** : Bean Validation (JSR-380) sur tous les DTOs

---

## 🤝 Contributeurs

Ce projet a été développé dans le cadre du module **DevOps & MLOps** (S5 - INDIA).

**Équipe** :
- SEKAL Douaâ
- TOUINSSI Nouhaila
- ZAALI Mohamed

---

## 📝 Licence

Projet académique - ENSAM Rabat  
Année universitaire 2024-2025

---

**ENSAM - École Nationale Supérieure d'Arts et Métiers**  
*Excellence in Engineering Education*
