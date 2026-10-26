package com.example.burserker.beopdong.global.security.jwt;

import com.example.burserker.beopdong.domain.organ.domain.RefreshToken;
import com.example.burserker.beopdong.domain.organ.domain.repository.OrganRepository;
import com.example.burserker.beopdong.domain.organ.domain.repository.RefreshTokenRepository;
import com.example.burserker.beopdong.domain.organ.exception.OrganNotFoundException;
import com.example.burserker.beopdong.domain.organ.presentation.dto.response.TokenResponse;
import com.example.burserker.beopdong.domain.user.exception.ExpiredTokenException;
import com.example.burserker.beopdong.domain.user.exception.InvalidTokenException;
import com.example.burserker.beopdong.global.security.auth.CustomUserDetailsService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    private final JwtProperties jwtProperties;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CustomUserDetailsService customUserDetailsService;
    private final OrganRepository organRepository;

    private static final String CLAIM_TYPE = "type";
    private static final String ACCESS_TYPE = "access";
    private static final String REFRESH_TYPE = "refresh";
    private static final int MILLISECONDS = 1000;

    public String createAccessToken(String organName) {
        Date now = new Date();

        return Jwts.builder()
                .setSubject(organName)
                .claim(CLAIM_TYPE, ACCESS_TYPE)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + jwtProperties.getAccessExpiration() * MILLISECONDS))
                .signWith(SignatureAlgorithm.HS512, jwtProperties.getSecretKey())
                .compact();
    }

    public String createRefreshToken(String organName) {
        Date now = new Date();

        String refreshToken = Jwts.builder()
                .claim(CLAIM_TYPE, REFRESH_TYPE)
                .setIssuedAt(now)
                .setExpiration(new java.sql.Timestamp(now.getTime() + jwtProperties.getRefreshExpiration() * MILLISECONDS))
                .signWith(SignatureAlgorithm.HS512, jwtProperties.getSecretKey())
                .compact();

        refreshTokenRepository.save(
                RefreshToken.builder()
                        .organName(organName)
                        .token(refreshToken)
                        .timeToLive((jwtProperties.getRefreshExpiration()))
                        .build()
        );

        return refreshToken;
    }

    public Authentication getAuthentication(String token) {

        Claims claims = getClaims(token);

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(claims.getSubject());

        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public Claims getClaims(String token) {

        try {
            return Jwts
                    .parser()
                    .setSigningKey(jwtProperties.getSecretKey())
                    .parseClaimsJws(token)
                    .getBody();
        }
        catch (ExpiredJwtException e) {
            throw ExpiredTokenException.EXCEPTION;
        }
        catch (Exception E) {
            throw InvalidTokenException.EXCEPTION;
        }
    }

    public TokenResponse receiveToken(String organName) {

        organRepository.findByOrganName(organName)
                .orElseThrow(() -> OrganNotFoundException.EXCEPTION);

        return TokenResponse.of(
                createAccessToken(organName),
                createRefreshToken(organName),
                organName
        );
    }

    public String resolveToken(HttpServletRequest request) {

        String bearerToken = request.getHeader(jwtProperties.getHeader());

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(jwtProperties.getPrefix())
                && bearerToken.length() > jwtProperties.getPrefix().length() + 1) {
            return bearerToken.substring(7);
        }

        return null;
    }
}