package com.product.config.jwt;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtUtil.class);
    private static final Duration TOKEN_LIFETIME = Duration.ofHours(1);

    private final SecretKey signingKey;
    private final JwtParser parser;

    public JwtUtil(@Value("${app.jwt.secret}") String secret) {
        signingKey = resolveSigningKey(secret);
        parser = Jwts.parser().verifyWith(signingKey).build();
    }

    public String issue(String username, Long userId, String role) {
        Instant issuedAt = Instant.now();
        return Jwts.builder()
                .subject(username)
                .claim("id", userId)
                .claim("roles", List.of(role))
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(issuedAt.plus(TOKEN_LIFETIME)))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    private SecretKey resolveSigningKey(String secret) {
        if (secret.isEmpty()) {
            LOGGER.warn("JWT_SECRET sin configurar. Los JWT locales vencerán al reiniciar Product.");
            return Jwts.SIG.HS256.key().build();
        }

        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (!StringUtils.hasText(secret) || secretBytes.length < 32) {
            throw new IllegalArgumentException("JWT_SECRET debe contener al menos 32 bytes de texto UTF-8");
        }
        return Keys.hmacShaKeyFor(secretBytes);
    }

    Identity validate(String token) {
        Claims claims = parser.parseSignedClaims(token).getPayload();
        if (!StringUtils.hasText(claims.getSubject()) || claims.getExpiration() == null) {
            throw new MalformedJwtException("El JWT debe incluir sub y exp");
        }

        List<?> roles = claims.get("roles", List.class);
        List<GrantedAuthority> authorities = roles == null
                ? List.of()
                : roles.stream().map(this::toAuthority).toList();

        return new Identity(claims.getSubject(), claims.get("id", Long.class), authorities);
    }

    private GrantedAuthority toAuthority(Object role) {
        Object name = role instanceof Map<?, ?> attributes ? attributes.get("authority") : role;
        if (!(name instanceof String authority) || !StringUtils.hasText(authority)) {
            throw new MalformedJwtException("El JWT contiene un rol inválido");
        }
        return new SimpleGrantedAuthority(authority);
    }

    record Identity(String username, Long id, List<GrantedAuthority> authorities) {
    }
}
