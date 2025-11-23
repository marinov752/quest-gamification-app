-- SQL script to create the databases for Quest Gamification application
-- Run this script as a PostgreSQL superuser (usually 'postgres')

-- Create database for main application
CREATE DATABASE quest_gamification_db;

-- Create database for analytics microservice
CREATE DATABASE quest_analytics_db;

-- Grant privileges (optional, adjust as needed)
-- GRANT ALL PRIVILEGES ON DATABASE quest_gamification_db TO postgres;
-- GRANT ALL PRIVILEGES ON DATABASE quest_analytics_db TO postgres;

