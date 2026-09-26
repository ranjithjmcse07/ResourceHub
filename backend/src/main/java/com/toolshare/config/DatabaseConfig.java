package com.toolshare.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
public class DatabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    @Value("${spring.datasource.url:jdbc:mysql://localhost:3306/toolshare?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true}")
    private String configuredUrl;

    @Value("${spring.datasource.username:root}")
    private String configuredUsername;

    @Value("${spring.datasource.password:Ranjith@567}")
    private String configuredPassword;

    @Bean
    @Primary
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");

        // Check if DATABASE_URL or SPRING_DATASOURCE_URL is provided in cloud format (mysql://...)
        String rawUrl = System.getenv("DATABASE_URL");
        if (rawUrl == null || rawUrl.isBlank()) {
            rawUrl = configuredUrl;
        }

        if (rawUrl != null && rawUrl.startsWith("mysql://")) {
            try {
                log.info("Detected cloud MySQL URL format (mysql://), parsing credentials...");
                URI uri = new URI(rawUrl);
                String userInfo = uri.getUserInfo();
                String username = configuredUsername;
                String password = configuredPassword;

                if (userInfo != null && userInfo.contains(":")) {
                    String[] parts = userInfo.split(":", 2);
                    username = parts[0];
                    password = parts[1];
                }

                String host = uri.getHost();
                int port = uri.getPort() == -1 ? 3306 : uri.getPort();
                String path = uri.getPath();
                if (path == null || path.isBlank() || path.equals("/") || path.equalsIgnoreCase("/sys") || path.equalsIgnoreCase("/mysql") || path.equalsIgnoreCase("/information_schema") || path.equalsIgnoreCase("/performance_schema") || path.equalsIgnoreCase("/test")) {
                    path = "/toolshare";
                }
                String query = uri.getQuery();

                String jdbcUrl = "jdbc:mysql://" + host + ":" + port + path;
                if (query != null && !query.isBlank()) {
                    jdbcUrl += "?" + query;
                    if (!jdbcUrl.contains("createDatabaseIfNotExist")) {
                        jdbcUrl += "&createDatabaseIfNotExist=true";
                    }
                } else {
                    jdbcUrl += "?createDatabaseIfNotExist=true&sslMode=VERIFY_IDENTITY&useSSL=true&allowPublicKeyRetrieval=true";
                }

                config.setJdbcUrl(jdbcUrl);
                config.setUsername(username);
                config.setPassword(password);
                log.info("Configured JDBC URL: {} with user: {}", jdbcUrl, username);
            } catch (Exception e) {
                log.error("Failed to parse cloud mysql:// URL: {}, falling back to configured properties", e.getMessage());
                config.setJdbcUrl(configuredUrl);
                config.setUsername(configuredUsername);
                config.setPassword(configuredPassword);
            }
        } else {
            config.setJdbcUrl(configuredUrl);
            config.setUsername(configuredUsername);
            config.setPassword(configuredPassword);
        }

        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setIdleTimeout(30000);
        config.setConnectionTimeout(30000);

        return new HikariDataSource(config);
    }
}
