package com.flowra.flowra_backend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class RazorpayConfig {

    public static final String DEFAULT_TEST_KEY_ID = "rzp_test_TdVV3x1sj1qTZy";
    public static final String DEFAULT_TEST_KEY_SECRET = "j8Z36S1edgCPztljJuXTxTcG";

    @Value("${razorpay.key.id:}")
    private String keyId;

    @Value("${razorpay.key.secret:}")
    private String keySecret;

    @Value("${razorpay.currency:INR}")
    private String currency;

    @Value("${razorpay.company.name:FLOWRA Artisanal Florist}")
    private String companyName;

    public String getKeyId() {
        if (keyId == null || keyId.isBlank() || keyId.contains("YOUR_") || keyId.contains("5173FlowraKey") || keyId.contains("your_")) {
            return DEFAULT_TEST_KEY_ID;
        }
        return keyId.trim();
    }

    public String getKeySecret() {
        if (keySecret == null || keySecret.isBlank() || keySecret.contains("YOUR_") || keySecret.contains("FlowraSecretKey") || keySecret.contains("your_")) {
            return DEFAULT_TEST_KEY_SECRET;
        }
        return keySecret.trim();
    }

    public String getCurrency() {
        return currency != null && !currency.isBlank() ? currency : "INR";
    }

    public String getCompanyName() {
        return companyName != null && !companyName.isBlank() ? companyName : "FLOWRA Artisanal Florist";
    }

    public String getMaskedKeyId() {
        String active = getKeyId();
        if (active.length() < 8) return "****";
        return active.substring(0, 4) + "..." + active.substring(active.length() - 4);
    }
}
