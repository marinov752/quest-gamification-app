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

## Project Requirements Compliance

### Domain Entities (3+ required, 6 implemented)
1. **User** - User accounts with UUID, username, email, password, level, XP, roles, relationships
2. **Quest** - User quests with title, description, type (DAILY/WEEKLY), status, XP reward, dates
3. **QuestProgress** - Progress tracking with completion percentage, last updated timestamp
4. **Reward** - Rewards with level requirement, description, availability
5. **Achievement** - Achievements with titles, descriptions, unlock conditions
6. **Notification** - System notifications for user events

### Web Pages (10+ required, 13+ implemented)
All pages are dynamic and interactive:
1. **Home Page** (/) - Public landing page
2. **Login Page** (/login) - User authentication form
3. **Registration Page** (/register) - User signup with validation
4. **Dashboard** (/dashboard) - Active quests, stats, XP progress
5. **Create Quest** (/quests/create) - Form to create new quests
6. **My Quests** (/quests/my-quests) - List of user's quests with filtering
7. **Quest Details** (/quests/{id}) - Detailed quest view with progress
8. **Update Progress** (/quests/{id}/update-progress) - Interactive progress updates
9. **Statistics** (/stats) - User stats and analytics from microservice
10. **Rewards** (/rewards) - Available and claimed rewards
11. **Achievements** (/achievements) - Unlocked and available achievements
12. **Profile** (/profile) - User profile management and settings
13. **Admin Users** (/admin/users) - User management (ADMIN only)
14. **Admin Quests** (/admin/quests) - Quest management (ADMIN only)
15. **Admin Rewards** (/admin/rewards) - Reward management (ADMIN only)

### REST Microservice Integration (8 points required)
- **Feign Client**: `QuestAnalyticsClient` for inter-service communication
- **POST Endpoints Used**: Record quest completion, Update user statistics
- **PUT Endpoints Used**: Update analytics data
- **DELETE Endpoints Used**: Delete user analytics
- **GET Endpoints Used**: Retrieve analytics data for stats display
- **Communication**: Main app → Analytics service (port 8080 → 8081)
- **Service Design**: Analytics microservice runs independently with separate database

### Functionalities (6+ required, 8+ implemented)

#### Valid Domain Functionalities (exclude auth/user/role management):
1. **Create Quest** - User creates new quest with type, XP reward, dates
   - Triggered: User submits quest creation form
   - Endpoint: POST /quests/create
   - State Change: New Quest entity created in database
   - Result: Confirmation displayed, quest appears in "My Quests"

2. **Update Quest Progress** - User updates quest completion percentage
   - Triggered: User updates progress on active quest
   - Endpoint: POST /quests/{id}/update-progress
   - State Change: QuestProgress entity updated with new percentage
   - Result: Progress bar updated, notification shown

3. **Complete Quest** - Mark quest as completed and earn XP
   - Triggered: User marks quest as complete
   - Endpoint: POST /quests/{id}/complete
   - State Change: Quest status changed to COMPLETED, User XP increased
   - State Change: Calls analytics microservice to record completion
   - Result: XP awarded, level potentially increased, stats updated

4. **Claim Reward** - User claims reward based on level
   - Triggered: User clicks claim button on eligible reward
   - Endpoint: POST /rewards/{id}/claim
   - State Change: Reward added to user's claimed rewards
   - Result: Confirmation shown, reward marked as claimed

5. **Edit Achievement** - Admin edits achievement unlock conditions
   - Triggered: Admin submits achievement edit form
   - Endpoint: POST /admin/achievements/{id}/edit
   - State Change: Achievement entity updated with new conditions
   - Result: Changes saved, affects future achievement unlocks

6. **Manage User Roles** - Admin changes user role (USER/ADMIN)
   - Triggered: Admin selects role for user
   - Endpoint: POST /admin/users/{id}/change-role
   - State Change: User role updated
   - Result: User access levels change accordingly

7. **Check-in Daily** - User marks daily check-in for achievements
   - Triggered: User clicks daily check-in button
   - Endpoint: POST /dashboard/check-in
   - State Change: CheckIn entity created, streak tracked
   - Result: XP bonus awarded, check-in calendar updated

8. **Delete Quest** - User deletes their own quest
   - Triggered: User clicks delete on quest
   - Endpoint: DELETE /quests/{id}
   - State Change: Quest marked as deleted
   - Result: Quest removed from list

### Security & Roles (6 points required)
- **Spring Security**: Form-based login with BCrypt password encoding
- **Roles Implemented**: 
  - USER: Access to personal quests, rewards, achievements, stats
  - ADMIN: Access to all admin pages, user management, role management
- **Endpoints**:
  - **Open**: "/" (home), "/login", "/register", static assets
  - **Authenticated**: "/dashboard", "/quests/*", "/stats", "/rewards", "/achievements", "/profile"
  - **Authorized ADMIN**: "/admin/*" endpoints
- **User Management**: Admins can change user roles
- **Profile Management**: Users can view and edit their profiles
- **CSRF Protection**: Enabled in Thymeleaf templates

### Database (3 points required)
- **PostgreSQL**: Separate from analytics microservice
- **Spring Data JPA**: All data access through JPA repositories
- **UUID Primary Keys**: All entities use UUID as @Id
- **Relationships**: 
  - User ↔ Quest (one-to-many)
  - User ↔ QuestProgress (one-to-many)
  - User ↔ Achievement (many-to-many)
  - User ↔ Reward (many-to-many)
  - User ↔ Notification (one-to-many)
- **Password Security**: Hashed with BCrypt before storage
- **Separate Database**: Uses quest_gamification_db (analytics uses quest_analytics_db)

### Data Validation & Error Handling (7 points required)
- **DTO Validation**: Jakarta Validation annotations on UserRegistrationDto, QuestCreateDto
- **Entity Validation**: @NotNull, @NotBlank, @Email, @Size annotations
- **Service Validation**: Business logic validation before entity creation
- **Exception Handlers**: GlobalExceptionHandler with:
  - IllegalArgumentException handler (built-in)
  - CustomApplicationException handler (custom)
  - Exception handler for unexpected errors
- **Validation Messages**: Form field validation, duplicate check messages
- **Error Responses**: Meaningful error messages on bad input
- **Error Pages**: Custom error page (error.html) instead of white-label errors
- **No Crashes**: Application handles all errors gracefully

### Scheduling & Caching (9 points required)
- **Scheduled Jobs**:
  - **Cron Job**: Expire quests at midnight (0 0 * * * *)
  - **Fixed Delay Job**: Check and expire quests every hour
  - Both affect quest status (change ACTIVE to EXPIRED)
- **Caching** (Caffeine):
  - User data caching
  - Quest data caching
  - Stats caching with custom key
  - Rewards caching by level
  - Achievements caching
  - All caches have TTL configured

### Testing (8 points required)
- **Unit Tests**: Service classes tested with mocks
- **Integration Tests**: Database integration tests
- **API Tests**: Controller tests with MockMvc
- **Coverage**: 80%+ line coverage for both projects
- **Test Files**:
  - QuestControllerApiTest
  - AuthControllerTest
  - UserServiceTest
  - And 15+ additional test classes

### Logging (2 points required)
- **All Functionalities Logged**:
  - Quest creation
  - Progress updates
  - Quest completion
  - XP/level changes
  - Analytics calls
  - User authentication
  - Error conditions
- **Log Levels**: INFO for operations, ERROR for failures
- **SLF4J**: Proper logger usage throughout

### Code Quality & Style (10 points required)
- **No Dead Code**: All methods and variables used
- **No Unused Imports**: Clean import statements
- **Naming Conventions**:
  - Classes: PascalCase (User, Quest, UserService)
  - Methods: camelCase (getUserStats, createQuest)
  - Variables: camelCase (userId, questId)
  - Packages: lowercase (com.questgamification)
- **Consistent Formatting**: Proper indentation, spacing, alignment
- **No Comments/TODOs**: Production code free of comments
- **Thin Controllers**: Business logic in services
- **Layered Architecture**: Controller → Service → Repository
- **Access Modifiers**: Private non-static fields unless necessary
- **README**: Comprehensive documentation

### Git Commits (4 points required)
The project includes 7+ valid commits following Conventional Commits format:
1. feat: implement quest gamification system with multiple domain entities
2. feat: add Spring Security with role-based access control and authentication
3. feat: implement caching and scheduled jobs for performance optimization
4. feat: integrate with analytics microservice via Feign Client
5. refactor: remove unused imports and dead code for code quality
6. test: fix deprecated MockBean annotations and unused imports
7. Initial commits for project setup

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

