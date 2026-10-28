package com.example.burserker.beopdong.global.security.jwt;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import java.nio.charset.StandardCharsets;

import java.util.Base64;

@Getter
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private final String header;
    private final String prefix;
    private final String secretKey;
    private final Long accessExpiration;
    private final Long refreshExpiration;

    public JwtProperties(
            String header,
            String prefix,
            String secretKey,
            Long accessExpiration,
            Long refreshExpiration
    ) {
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException("JWT 비밀키가 설정되지 않았습니다.");
        }

        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);

        if (keyBytes.length < 64) {
            throw new IllegalStateException(
                    "HS512 JWT 비밀키는 64바이트 이상이어야 합니다."
            );
        }

        this.header = header;
        this.prefix = prefix;
        this.secretKey = Base64.getEncoder().encodeToString(keyBytes);
        this.accessExpiration = accessExpiration;
        this.refreshExpiration = refreshExpiration;
    }
}