# Database Setup Instructions

## Quick Setup

The application requires two PostgreSQL databases to be created before running:

1. `quest_gamification_db` - Main application database
2. `quest_analytics_db` - Analytics microservice database

## Method 1: Using psql Command Line (Recommended)

1. Open PowerShell or Command Prompt
2. Run the following command:

```powershell
psql -U postgres -c "CREATE DATABASE quest_gamification_db;"
psql -U postgres -c "CREATE DATABASE quest_analytics_db;"
```

If prompted for a password, enter your PostgreSQL password (default is usually `postgres`).

## Method 2: Using SQL File

1. Open PowerShell or Command Prompt
2. Navigate to the project directory
3. Run:

```powershell
psql -U postgres -f create-database.sql
```

## Method 3: Using pgAdmin (GUI)

1. Open pgAdmin
2. Connect to your PostgreSQL server
3. Right-click on "Databases" → "Create" → "Database"
4. Create database `quest_gamification_db`
5. Repeat for `quest_analytics_db`

## Method 4: Interactive psql Session

1. Open PowerShell or Command Prompt
2. Connect to PostgreSQL:

```powershell
psql -U postgres
```

3. Run the SQL commands:

```sql
CREATE DATABASE quest_gamification_db;
CREATE DATABASE quest_analytics_db;
\q
```

## Verify Database Creation

After creating the databases, verify they exist:

```powershell
psql -U postgres -c "\l" | findstr quest
```

You should see both databases listed.

## Troubleshooting

### "psql: command not found"
- Make sure PostgreSQL is installed and added to PATH
- Or use the full path to psql.exe (usually in `C:\Program Files\PostgreSQL\[version]\bin\`)

### "FATAL: password authentication failed"
- Check your PostgreSQL username and password in `application.yml`
- Default username: `postgres`
- Default password: `postgres` (change this in production!)

### Still getting "database does not exist" error?
- Make sure PostgreSQL service is running
- Check the database name matches exactly: `quest_gamification_db`
- Verify you can connect to PostgreSQL: `psql -U postgres -l`

