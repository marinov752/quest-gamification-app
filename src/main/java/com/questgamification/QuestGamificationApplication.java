package com.questgamification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;

@SpringBootApplication
@EnableCaching
@EnableScheduling
@EnableFeignClients
public class QuestGamificationApplication {

    private static final Logger logger = LoggerFactory.getLogger(QuestGamificationApplication.class);
    
    static {
        try {
            System.out.println("=== Starting database auto-creation... ===");
            initializeDatabase();
        } catch (Exception e) {
            System.err.println("=== CRITICAL ERROR in static initializer ===");
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(QuestGamificationApplication.class, args);
    }

    @SuppressWarnings("unchecked")
    private static void initializeDatabase() {
        try {
            System.out.println("Loading application.yml...");
            Yaml yaml = new Yaml();
            InputStream inputStream = QuestGamificationApplication.class
                    .getClassLoader()
                    .getResourceAsStream("application.yml");
            
            if (inputStream == null) {
                System.out.println("WARNING: Could not find application.yml, using defaults");
                createDatabase("quest_gamification_db", "postgres", "postgres");
                return;
            }
            
            System.out.println("Parsing application.yml...");
            Map<String, Object> config = yaml.load(inputStream);
            Map<String, Object> spring = (Map<String, Object>) config.get("spring");
            Map<String, Object> datasource = (Map<String, Object>) spring.get("datasource");
            
            String url = (String) datasource.get("url");
            String username = (String) datasource.getOrDefault("username", "postgres");
            String password = (String) datasource.getOrDefault("password", "postgres");
            
            System.out.println("Database URL: " + url);
            
            if (url != null && url.contains("postgresql")) {
                String databaseName = extractDatabaseName(url);
                String baseUrl = url.substring(0, url.lastIndexOf('/'));
                String postgresUrl = baseUrl + "/postgres";
                
                System.out.println("Target database: " + databaseName);
                System.out.println("PostgreSQL base URL: " + postgresUrl);
                
                createDatabase(databaseName, username, password, postgresUrl);
            } else {
                System.out.println("Skipping - not a PostgreSQL database");
            }
            
            inputStream.close();
        } catch (Exception e) {
            System.err.println("✗ ERROR: Failed to auto-create database: " + e.getMessage());
            System.err.println("Stack trace:");
            e.printStackTrace();
            System.err.println("Please create the database manually: CREATE DATABASE quest_gamification_db;");
        }
    }

    private static void createDatabase(String databaseName, String username, String password) {
        createDatabase(databaseName, username, password, "jdbc:postgresql://localhost:5432/postgres");
    }

    private static void createDatabase(String databaseName, String username, String password, String postgresUrl) {
        try {
            System.out.println("=== Auto-creating database '" + databaseName + "' if it doesn't exist... ===");
            logger.info("Auto-creating database '{}' if it doesn't exist...", databaseName);
            
            try (Connection connection = DriverManager.getConnection(postgresUrl, username, password);
                 Statement statement = connection.createStatement()) {
                
                String checkDbQuery = "SELECT 1 FROM pg_database WHERE datname = '" + databaseName + "'";
                ResultSet resultSet = statement.executeQuery(checkDbQuery);
                
                if (!resultSet.next()) {
                    System.out.println("Database '" + databaseName + "' does not exist. Creating it...");
                    logger.info("Database '{}' does not exist. Creating it...", databaseName);
                    statement.executeUpdate("CREATE DATABASE " + databaseName);
                    System.out.println("✓ Database '" + databaseName + "' created successfully!");
                    logger.info("✓ Database '{}' created successfully!", databaseName);
                } else {
                    System.out.println("✓ Database '" + databaseName + "' already exists.");
                    logger.info("✓ Database '{}' already exists.", databaseName);
                }
                resultSet.close();
            }
        } catch (Exception e) {
            System.err.println("✗ ERROR: Failed to create database '" + databaseName + "': " + e.getMessage());
            System.err.println("Please create the database manually: CREATE DATABASE " + databaseName + ";");
            logger.error("✗ Failed to create database '{}': {}", databaseName, e.getMessage(), e);
        }
    }

    private static String extractDatabaseName(String url) {
        int lastSlash = url.lastIndexOf('/');
        int questionMark = url.indexOf('?', lastSlash);
        
        if (questionMark != -1) {
            return url.substring(lastSlash + 1, questionMark);
        }
        return url.substring(lastSlash + 1);
    }
}

