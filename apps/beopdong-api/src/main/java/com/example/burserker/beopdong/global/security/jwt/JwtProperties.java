package com.example.burserker.beopdong.global.security.jwt;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Base64;

@Getter
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private final String header;
    private final String prefix;
    private final String secretKey;
    private final Long accessExpiration;
    private final Long refreshExpiration;

    public JwtProperties(String header, String prefix, String secretKey, Long accessExpiration, Long refreshExpiration) {
        this.header = header;
        this.prefix = prefix;
        this.secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
        this.accessExpiration = accessExpiration;
        this.refreshExpiration = refreshExpiration;
    }
}