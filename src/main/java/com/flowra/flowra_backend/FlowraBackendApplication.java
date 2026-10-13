package com.flowra.flowra_backend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.InetSocketAddress;
import java.net.Socket;

@SpringBootApplication
public class FlowraBackendApplication {

    private static final Logger log = LoggerFactory.getLogger(FlowraBackendApplication.class);

    private static final String RAILWAY_PROXY_HOST = "66.33.22.253";
    private static final int RAILWAY_PROXY_PORT = 19487;
    private static final String RAILWAY_JDBC_URL = "jdbc:mysql://66.33.22.253:19487/railway?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata";

    public static void main(String[] args) {
        // Enforce IPv4 priority across all JVM socket and DNS operations
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("java.net.preferIPv4Addresses", "true");

        String dbUrl = System.getenv("DB_URL");
        String dbUser = System.getenv("DB_USERNAME");
        String port = System.getenv("PORT");

        log.info("==================================================================");
        log.info("🌸 FLOWRA BACKEND STARTUP INITIALIZATION");
        log.info("🌸 Active Port (PORT): {}", port != null ? port : "8080 (default)");
        log.info("🌸 Database User (DB_USERNAME): {}", dbUser != null ? dbUser : "[NOT SET]");
        log.info("🌸 Configured DB_URL: {}", sanitize(dbUrl));

        if (dbUrl != null && !dbUrl.isBlank()) {
            if (dbUrl.contains("caboose.proxy.rlwy.net")) {
                log.info("🌸 Railway TCP proxy hostname detected. Verifying IPv4 connectivity to {}:{}...", RAILWAY_PROXY_HOST, RAILWAY_PROXY_PORT);
                if (probeSocket(RAILWAY_PROXY_HOST, RAILWAY_PROXY_PORT)) {
                    log.info("🌸 Routing to direct IPv4 address to bypass container DNS/IPv6 issues.");
                    String ipv4Url = dbUrl.replace("caboose.proxy.rlwy.net", RAILWAY_PROXY_HOST);
                    System.setProperty("spring.datasource.url", ipv4Url);
                    System.setProperty("DB_URL", ipv4Url);
                }
            } else if (dbUrl.contains("localhost") || dbUrl.contains("127.0.0.1")) {
                boolean localReachable = probeUrlSocket(dbUrl);
                if (!localReachable) {
                    log.warn("⚠️ localhost MySQL is not reachable in this cloud container.");
                    log.info("🌸 Checking Railway MySQL TCP proxy ({}:{})...", RAILWAY_PROXY_HOST, RAILWAY_PROXY_PORT);
                    if (probeSocket(RAILWAY_PROXY_HOST, RAILWAY_PROXY_PORT)) {
                        log.warn("🌸 AUTOMATIC FALLBACK: Switching DB_URL to Railway proxy at {}:{}/railway.", RAILWAY_PROXY_HOST, RAILWAY_PROXY_PORT);
                        System.setProperty("spring.datasource.url", RAILWAY_JDBC_URL);
                        System.setProperty("DB_URL", RAILWAY_JDBC_URL);
                    }
                }
            } else {
                probeUrlSocket(dbUrl);
            }
        } else {
            log.warn("⚠️ DB_URL environment variable is NOT set!");
            if (probeSocket(RAILWAY_PROXY_HOST, RAILWAY_PROXY_PORT)) {
                log.warn("🌸 AUTOMATIC FALLBACK: Setting DB_URL to Railway proxy at {}:{}/railway.", RAILWAY_PROXY_HOST, RAILWAY_PROXY_PORT);
                System.setProperty("spring.datasource.url", RAILWAY_JDBC_URL);
                System.setProperty("DB_URL", RAILWAY_JDBC_URL);
            }
        }
        log.info("==================================================================");

        SpringApplication.run(FlowraBackendApplication.class, args);
    }

    private static boolean probeSocket(String host, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 4000);
            log.info("✅ TCP socket connection to {}:{} SUCCEEDED!", host, port);
            return true;
        } catch (Exception e) {
            log.error("❌ TCP socket connection to {}:{} FAILED: {}", host, port, e.getMessage());
            return false;
        }
    }

    private static boolean probeUrlSocket(String url) {
        try {
            if (url.startsWith("jdbc:mysql://")) {
                String sub = url.substring("jdbc:mysql://".length());
                int slash = sub.indexOf('/');
                if (slash > 0) sub = sub.substring(0, slash);
                int at = sub.indexOf('@');
                if (at >= 0) sub = sub.substring(at + 1);
                String[] parts = sub.split(":");
                String host = parts[0].trim();
                int port = parts.length > 1 ? Integer.parseInt(parts[1].trim()) : 3306;
                return probeSocket(host, port);
            }
        } catch (Exception e) {
            log.warn("Socket probe warning: {}", e.getMessage());
        }
        return false;
    }

    private static String sanitize(String url) {
        if (url == null) return "[NULL]";
        return url.replaceAll("://([^:]+):([^@]+)@", "://$1:****@");
    }
}
