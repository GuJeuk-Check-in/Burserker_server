package com.example.burserker.gujeuk.global.config;

import com.example.burserker.gujeuk.global.security.implementation.JwtAuthenticationEntryPoint;
import com.example.burserker.gujeuk.global.security.jwt.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final ObjectMapper objectMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Value("${cors.allowed-origins.prod-url}")
    private String prodUrl;

    @Value("${cors.allowed-origins.stag-url}")
    private String stagUrl;

    @Value("${cors.allowed-origins.test-url}")
    private String testUrl;

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    protected SecurityFilterChain configure(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .sessionManagement(sessionManagement -> sessionManagement
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/common/analytics/check-in-funnel",
                                "/common/analytics/check-in-funnel/**"
                        ).hasRole("ORGAN")
                        .requestMatchers(
                                "/user",
                                "/user/**",
                                "/organ/create",
                                "/organ/login",
                                "/purpose/all",
                                "/residence/all",
                                "/user/ha-log", "/user/ha-sign-up",
                                "/common/health/ready"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exceptionHandling -> exceptionHandling
                    .authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .with(new SecurityFilterConfig(jwtTokenProvider, objectMapper), Customizer.withDefaults())
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(allowedOrigins());

        configuration.setAllowedMethods(Arrays.asList("OPTIONS", "GET", "POST", "PUT", "PATCH", "DELETE"));
        configuration.addAllowedHeader("*");
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    private List<String> allowedOrigins() {
        List<String> origins = new ArrayList<>();

        addOrigins(origins, prodUrl);
        addOrigins(origins, stagUrl);
        addOrigins(origins, testUrl);
        origins.add("http://localhost:5175");
        origins.add("http://localhost:5176");
        origins.add("https://gujeuk-check-in-user.pages.dev");
        origins.add("https://gujeuk-check-stage-user.pages.dev");

        return origins;
    }

    private void addOrigins(List<String> origins, String originValues) {
        if (originValues == null) {
            return;
        }

        Arrays.stream(originValues.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .map(this::removeTrailingSlash)
                .forEach(origins::add);
    }

    private String removeTrailingSlash(String origin) {
        while (origin.endsWith("/")) {
            origin = origin.substring(0, origin.length() - 1);
        }

        return origin;
    }
}
