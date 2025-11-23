# Quest Gamification App

## Overview
Quest Gamification App is a web application that allows users to turn real-life tasks into daily and weekly quests. Users can track their progress, earn experience points (XP), level up, unlock achievements, and claim rewards.

## Technology Stack
- **Java**: 17
- **Spring Boot**: 3.4.0
- **Build Tool**: Maven
- **Database**: PostgreSQL
- **Frontend**: Spring MVC + Thymeleaf
- **Security**: Spring Security
- **Caching**: Caffeine (Spring Cache)
- **Inter-Service Communication**: Feign Client

## Features

### Domain Entities
1. **User** - User accounts with levels, XP, roles
2. **Quest** - Daily/weekly quests with rewards
3. **QuestProgress** - Progress tracking for quests
4. **Reward** - Rewards that can be claimed by users
5. **Achievement** - Achievements unlocked by users

### Functionalities
1. **User Registration & Authentication** - Secure user registration and login
2. **Create Quest** - Create daily or weekly quests with XP rewards
3. **Update Quest Progress** - Track progress on active quests
4. **Complete Quest** - Mark quests as completed and earn XP
5. **Level Up System** - Automatic level calculation based on XP
6. **Claim Rewards** - Claim rewards based on level and XP
7. **View Statistics** - View user stats and analytics
8. **Achievement System** - Unlock achievements based on milestones
9. **Admin Panel** - Manage user roles (ADMIN only)

### Security
- Spring Security with role-based access control
- Two roles: USER and ADMIN
- Password encryption using BCrypt
- CSRF protection enabled
- Open endpoints: home, register, login
- Authenticated endpoints: dashboard, quests, stats, rewards
- Authorized endpoints: admin panel (ADMIN only)

### Database
- PostgreSQL database
- UUID primary keys for all entities
- JPA relationships between entities
- Password hashing for security

### Validation & Error Handling
- DTO validation with Jakarta Validation
- Entity validation
- Service-level validation
- Global exception handlers for:
  - IllegalArgumentException (built-in)
  - CustomApplicationException (custom)
  - Generic Exception handler

### Scheduling
- Daily cron job (midnight) to expire quests
- Hourly fixed delay job to check and expire quests

### Caching
- User data caching
- Quest data caching
- Stats caching
- Rewards caching
- Achievements caching

### Integration
- REST microservice integration via Feign Client
- Analytics service communication for quest completion tracking

## Web Pages
1. **Home** (/) - Landing page
2. **Login** (/login) - User login
3. **Register** (/register) - User registration
4. **Dashboard** (/dashboard) - User dashboard with stats and active quests
5. **Create Quest** (/quests/create) - Form to create new quests
6. **My Quests** (/quests/my-quests) - List of user's quests
7. **Quest Details** (/quests/{id}) - Detailed view of a quest
8. **Update Progress** (/quests/{id}/update-progress) - Update quest progress
9. **Stats** (/stats) - User statistics and analytics
10. **Rewards** (/rewards) - Available and claimed rewards
11. **Achievements** (/achievements) - User achievements
12. **Profile** (/profile) - User profile management
13. **Admin Users** (/admin/users) - User management (ADMIN only)

## Setup Instructions

### Prerequisites
- Java 17
- Maven 3.6+
- PostgreSQL 12+

### Database Setup

**Automatic Database Creation:**
The application will automatically create the database if it doesn't exist when you first run it. You just need to:
1. Make sure PostgreSQL is running
2. Ensure the `postgres` user has permission to create databases
3. Run the application - the database will be created automatically

**Manual Database Setup (if automatic creation fails):**
If automatic creation fails, you can create the database manually:
```sql
CREATE DATABASE quest_gamification_db;
```

**Schema Creation:**
The database schema (tables, columns, etc.) is automatically created/updated by Hibernate when the application starts (configured via `ddl-auto: update`).

**Update Configuration:**
Update `application.yml` with your database credentials if needed (default: username=`postgres`, password=`postgres`).

### Running the Application
1. Clone the repository
2. Navigate to the project directory
3. Run: `mvn spring-boot:run`
4. Access the application at: `http://localhost:8080`

### Testing
Run tests with: `mvn test`

## Project Structure
```
src/
├── main/
│   ├── java/com/questgamification/
│   │   ├── config/          # Configuration classes
│   │   ├── controller/      # MVC controllers
│   │   ├── domain/
│   │   │   ├── dto/         # Data Transfer Objects
│   │   │   └── entity/      # JPA entities
│   │   ├── exception/       # Exception handlers
│   │   ├── repository/      # JPA repositories
│   │   ├── scheduler/       # Scheduled jobs
│   │   ├── service/         # Business logic services
│   │   └── QuestGamificationApplication.java
│   └── resources/
│       ├── templates/       # Thymeleaf templates
│       └── application.yml  # Application configuration
└── test/                    # Test classes
```

## Integration with Microservice
This application integrates with the Quest Analytics Service microservice using Feign Client:
- Records quest completions
- Updates user statistics
- Retrieves analytics data

The microservice runs on port 8081 and must be running for full functionality.

