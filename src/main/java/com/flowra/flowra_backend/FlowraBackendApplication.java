package com.flowra.flowra_backend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SpringBootApplication
public class FlowraBackendApplication {

    private static final Logger log = LoggerFactory.getLogger(FlowraBackendApplication.class);

    public static final String RAILWAY_PROXY_HOST = "66.33.22.253";
    public static final int RAILWAY_PROXY_PORT = 19487;
    public static final String RAILWAY_JDBC_URL = "jdbc:mysql://66.33.22.253:19487/railway?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata";
    public static final String RAILWAY_DEFAULT_USER = "root";
    public static final String RAILWAY_DEFAULT_PASSWORD = "wlpVinwcSfccfMzHAnWCapZwosuBcffQ";

    public static void main(String[] args) {
        // Enforce IPv4 priority across all JVM socket and DNS operations
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("java.net.preferIPv4Addresses", "true");

        String dbUrl = System.getenv("DB_URL");
        String dbUser = System.getenv("DB_USERNAME");
        String dbPass = System.getenv("DB_PASSWORD");
        String port = System.getenv("PORT");

        log.info("==================================================================");
        log.info("🌸 FLOWRA BACKEND STARTUP INITIALIZATION");
        log.info("🌸 Active Port (PORT): {}", port != null ? port : "8080 (default)");
        log.info("🌸 Database User (DB_USERNAME): {}", dbUser != null ? dbUser : "[NOT SET]");
        log.info("🌸 Configured DB_URL: {}", sanitize(dbUrl));

        boolean needsFallback = false;
        String effectiveDbUrl = dbUrl;

        if (dbUrl != null && !dbUrl.isBlank()) {
            if (dbUrl.contains("caboose.proxy.rlwy.net")) {
                log.info("🌸 Railway TCP proxy hostname detected. Verifying IPv4 connectivity to {}:{}...", RAILWAY_PROXY_HOST, RAILWAY_PROXY_PORT);
                if (probeSocket(RAILWAY_PROXY_HOST, RAILWAY_PROXY_PORT)) {
                    log.info("🌸 Routing to direct IPv4 address to bypass container DNS/IPv6 issues.");
                    effectiveDbUrl = dbUrl.replace("caboose.proxy.rlwy.net", RAILWAY_PROXY_HOST);
                }
            } else {
                boolean reachable = probeUrlSocket(dbUrl);
                if (!reachable) {
                    log.warn("⚠️ The configured DB_URL cannot be reached from this container: {}", sanitize(dbUrl));
                    needsFallback = true;
                }
            }
        } else {
            log.warn("⚠️ DB_URL environment variable is NOT set in environment!");
            needsFallback = true;
        }

        if (needsFallback) {
            log.info("🌸 Checking Railway public TCP proxy ({}:{})...", RAILWAY_PROXY_HOST, RAILWAY_PROXY_PORT);
            if (probeSocket(RAILWAY_PROXY_HOST, RAILWAY_PROXY_PORT)) {
                log.warn("🌸 AUTOMATIC FALLBACK: Switching DB_URL to Railway proxy at {}:{}/railway.", RAILWAY_PROXY_HOST, RAILWAY_PROXY_PORT);
                effectiveDbUrl = RAILWAY_JDBC_URL;
            } else {
                log.error("❌ Both configured DB_URL and Railway TCP proxy fallback failed to connect.");
            }
        }

        List<String> effectiveArgs = new ArrayList<>(Arrays.asList(args));

        if (effectiveDbUrl != null && !effectiveDbUrl.isBlank()) {
            System.setProperty("spring.datasource.url", effectiveDbUrl);
            System.setProperty("DB_URL", effectiveDbUrl);
            effectiveArgs.add("--spring.datasource.url=" + effectiveDbUrl);
            log.info("🌸 Effective Datasource URL applied: {}", sanitize(effectiveDbUrl));
        }

        if (dbUser == null || dbUser.isBlank() || dbUser.contains("<") || dbUser.contains("your_")) {
            effectiveArgs.add("--spring.datasource.username=" + RAILWAY_DEFAULT_USER);
            System.setProperty("spring.datasource.username", RAILWAY_DEFAULT_USER);
            System.setProperty("DB_USERNAME", RAILWAY_DEFAULT_USER);
            log.info("🌸 Active Database User: {}", RAILWAY_DEFAULT_USER);
        }

        if (dbPass == null || dbPass.isBlank() || dbPass.contains("<") || dbPass.contains("your_")) {
            effectiveArgs.add("--spring.datasource.password=" + RAILWAY_DEFAULT_PASSWORD);
            System.setProperty("spring.datasource.password", RAILWAY_DEFAULT_PASSWORD);
            System.setProperty("DB_PASSWORD", RAILWAY_DEFAULT_PASSWORD);
            log.info("🌸 Active Database Password applied from Railway config.");
        }

        log.info("==================================================================");

        SpringApplication.run(FlowraBackendApplication.class, effectiveArgs.toArray(new String[0]));
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
