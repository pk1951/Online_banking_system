package com.truebank.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.beans.factory.annotation.Value;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
public class DatabaseConfig {

    @Value("${spring.datasource.h2.url}")
    private String h2Url;

    @Value("${spring.datasource.h2.username}")
    private String h2Username;

    @Value("${spring.datasource.h2.password}")
    private String h2Password;

    @Value("${spring.datasource.h2.driver-class-name}")
    private String h2DriverClassName;
    
    @Value("${spring.datasource.url}")
    private String postgresUrl;

    @Value("${spring.datasource.username}")
    private String postgresUsername;

    @Value("${spring.datasource.password}")
    private String postgresPassword;

    @Value("${spring.datasource.driver-class-name}")
    private String postgresDriverClassName;
    
    @Value("${spring.datasource.hikari.maximum-pool-size:5}")
    private int maxPoolSize;
    
    @Value("${spring.datasource.hikari.minimum-idle:1}")
    private int minIdle;
    
    @Value("${spring.datasource.hikari.connection-timeout:10000}")
    private long connectionTimeout;

    @Bean
    @Profile("dev")
    public DataSource h2DataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(h2Url);
        dataSource.setUsername(h2Username);
        dataSource.setPassword(h2Password);
        dataSource.setDriverClassName(h2DriverClassName);
        return dataSource;
    }
    
    @Bean
    @Profile("prod")
    public DataSource postgresDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(postgresUrl);
        config.setUsername(postgresUsername);
        config.setPassword(postgresPassword);
        config.setDriverClassName(postgresDriverClassName);
        config.setMaximumPoolSize(maxPoolSize);
        config.setMinimumIdle(minIdle);
        config.setConnectionTimeout(connectionTimeout);
        
        // Important for Supabase transaction mode pooler
        config.setAutoCommit(true);
        config.addDataSourceProperty("prepareThreshold", "0"); // Disable prepared statements
        config.addDataSourceProperty("reWriteBatchedInserts", "true");
        
        Properties props = new Properties();
        props.setProperty("ApplicationName", "TrueBank");
        config.setDataSourceProperties(props);
        
        return new HikariDataSource(config);
    }
}
