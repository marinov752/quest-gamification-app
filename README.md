# Quest Gamification App

## Overview
Quest Gamification App is a web application that allows users to turn real-life tasks into daily and weekly quests. Users can track their progress, earn experience points (XP), level up, unlock achievements, and claim rewards. The application integrates with a separate analytics microservice for comprehensive statistics tracking.

## Technology Stack
- **Java**: 17
- **Spring Boot**: 3.4.0
- **Build Tool**: Maven
- **Database**: PostgreSQL (separate from microservice)
- **Frontend**: Spring MVC + Thymeleaf
- **Security**: Spring Security with role-based access control
- **Caching**: Caffeine (Spring Cache)
- **Inter-Service Communication**: Feign Client (OpenFeign)
- **Testing**: JUnit 5, Mockito
- **Logging**: SLF4J

## Features

### Domain Entities
1. **User** - User accounts with levels, XP, and role-based access
2. **Quest** - Daily/weekly quests with rewards and completion tracking
3. **QuestProgress** - Fine-grained progress tracking on quests
4. **Reward** - Level-gated rewards for users to claim
5. **Achievement** - Achievements unlocked through gameplay
6. **Notification** - System notifications for user events

### Core Functionalities

**Quest Management:**
- Create personal quests with custom details
- Update quest progress with detailed tracking
- Complete quests and earn XP
- Delete quests
- Automatic quest expiration

**Reward System:**
- Level-gated rewards
- Claim available rewards
- Track claimed rewards

**Achievement System:**
- Automatic achievement unlocking
- Track achievement progress
- Display achieved and pending achievements

**Stats & Analytics:**
- Real-time user statistics
- Integration with analytics microservice
- Detailed XP and level tracking
- Quest completion metrics

### Security Features
- Secure user authentication
- Password hashing with BCrypt
- Role-based access control (USER/ADMIN)
- CSRF protection
- Session management

## Web Pages & UI

### Public Pages
1. **Home** (/) - Landing page with overview
2. **Login** (/login) - User login form
3. **Register** (/register) - Registration with validation

### User Pages
4. **Dashboard** (/dashboard) - Overview with active quests and stats
5. **My Quests** (/quests/my-quests) - List of personal quests
6. **Create Quest** (/quests/create) - Quest creation form
7. **Quest Details** (/quests/{id}) - Full quest information
8. **Update Progress** (/quests/{id}/update-progress) - Progress tracking
9. **Statistics** (/stats) - User stats and microservice analytics
10. **Rewards** (/rewards) - Available and claimed rewards
11. **Achievements** (/achievements) - Achievement progress
12. **Profile** (/profile) - User profile management

### Admin Pages
13. **Admin Users** (/admin/users) - User management and role assignment
14. **Admin Quests** (/admin/quests) - Quest oversight
15. **Admin Rewards** (/admin/rewards) - Reward configuration

## Setup Instructions

### Prerequisites
- Java 17 or higher
- Maven 3.6 or higher
- PostgreSQL 12 or higher
- Quest Analytics Service running on port 8081

### Database Setup

**Automatic Database Creation:**
1. Ensure PostgreSQL is running
2. Default credentials in application.yml: postgres/postgres
3. Database created automatically on startup

**Manual Setup:**
```sql
CREATE DATABASE quest_gamification_db;
```

### Configuration
Edit `src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/quest_gamification_db
    username: postgres
    password: postgres
```

Also ensure `quest-analytics-service` is running on port 8081.

### Running the Application
```bash
cd quest-gamification-app
mvn clean spring-boot:run
```
Application runs on: `http://localhost:8080`

### Running Tests
```bash
mvn test
```

## Integration with Microservice
The Quest Gamification App integrates with Quest Analytics Service:
- Records quest completions via POST endpoint
- Updates user statistics via PUT endpoint
- Retrieves analytics data via GET endpoint
- Displays stats from microservice on stats page

Both applications run independently and communicate via REST API.

