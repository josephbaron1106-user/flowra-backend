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
            // Auto-fallback: Railway TCP proxy domain resolution on cloud container networks
            if (dbUrl.contains("caboose.proxy.rlwy.net")) {
                log.info("🌸 Railway TCP proxy detected. Verifying IPv4 connectivity to 66.33.22.253:19487...");
                boolean ipv4Reachable = probeSocket("66.33.22.253", 19487);
                if (ipv4Reachable) {
                    log.info("🌸 Setting direct IPv4 datasource URL to bypass container DNS/IPv6 issues.");
                    String ipv4Url = dbUrl.replace("caboose.proxy.rlwy.net", "66.33.22.253");
                    System.setProperty("spring.datasource.url", ipv4Url);
                }
            } else {
                probeUrlSocket(dbUrl);
            }
        } else {
            log.warn("⚠️ DB_URL environment variable is NOT set in the environment!");
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

    private static void probeUrlSocket(String url) {
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
                probeSocket(host, port);
            }
        } catch (Exception e) {
            log.warn("Socket probe warning: {}", e.getMessage());
        }
    }

    private static String sanitize(String url) {
        if (url == null) return "[NULL]";
        return url.replaceAll("://([^:]+):([^@]+)@", "://$1:****@");
    }
}
