package com.flowra.flowra_backend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

/**
 * Startup diagnostic logger for Render deployment and Railway database connectivity.
 * Verifies network interface binding and datasource connectivity without leaking credentials.
 */
@Slf4j
@Component
public class DatabaseStartupLogger {

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${spring.datasource.url:}")
    private String datasourceUrl;

    @Value("${spring.datasource.username:}")
    private String datasourceUsername;

    private final DataSource dataSource;

    public DatabaseStartupLogger(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("==================================================================");
        log.info("🌸 FLOWRA Backend started successfully on port: {}", serverPort);
        log.info("🌸 Database User: {}", datasourceUsername != null ? datasourceUsername : "[NOT SET]");
        log.info("🌸 Database URL (sanitized): {}", sanitizeJdbcUrl(datasourceUrl));

        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            log.info("✅ Database Connection SUCCESS: {} (Version: {})", 
                    metaData.getDatabaseProductName(), 
                    metaData.getDatabaseProductVersion());
            log.info("✅ Driver: {} (Version: {})", 
                    metaData.getDriverName(), 
                    metaData.getDriverVersion());
        } catch (Exception e) {
            log.error("❌ Database connection check failed: {}", e.getMessage());
        }
        log.info("==================================================================");
    }

    /**
     * Sanitizes database URL to ensure sensitive credentials are never logged.
     */
    private String sanitizeJdbcUrl(String url) {
        if (url == null || url.isBlank()) {
            return "[NOT SET]";
        }
        // Mask any inline passwords in credentials portion (e.g. //user:password@host)
        return url.replaceAll("://([^:]+):([^@]+)@", "://$1:****@");
    }
}
