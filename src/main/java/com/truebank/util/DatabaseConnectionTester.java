package com.truebank.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
@Profile("prod")
public class DatabaseConnectionTester implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConnectionTester.class);
    
    private final DataSource dataSource;
    
    @Autowired
    public DatabaseConnectionTester(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    @Override
    public void run(String... args) {
        log.info("Testing database connection...");
        try (Connection connection = dataSource.getConnection()) {
            log.info("Database connection successful: {}", connection.getMetaData().getURL());
            log.info("Database product name: {}", connection.getMetaData().getDatabaseProductName());
            log.info("Database product version: {}", connection.getMetaData().getDatabaseProductVersion());
        } catch (SQLException e) {
            log.error("Failed to connect to the database", e);
            log.error("Connection URL: {}", dataSource.toString());
            log.error("Error message: {}", e.getMessage());
            if (e.getCause() != null) {
                log.error("Caused by: {}", e.getCause().getMessage());
            }
        }
    }
} 