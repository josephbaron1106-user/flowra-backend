package com.flowra.flowra_backend.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@Getter
public class RazorpayConfig {

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    @Value("${razorpay.currency:INR}")
    private String currency;

    @Value("${razorpay.company.name:FLOWRA Artisanal Florist}")
    private String companyName;

    public String getMaskedKeyId() {
        if (keyId == null || keyId.length() < 8) return "****";
        return keyId.substring(0, 4) + "..." + keyId.substring(keyId.length() - 4);
    }
}
