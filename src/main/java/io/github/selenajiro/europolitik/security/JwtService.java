package io.github.selenajiro.europolitik.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    static final String DEV_SECRET_PREFIX = "dev-only-";

    private static final int MIN_SECRET_BYTES = 32;
    private static final long EXPIRATION_MS = 1000L * 60 * 60 * 24; // 24 hours

    private final SecretKey key;

    public JwtService(@Value("${europolitik.jwt.secret}") String secret, Environment environment) {
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "europolitik.jwt.secret must be at least " + MIN_SECRET_BYTES + " bytes long");
        }
        if (environment.acceptsProfiles(Profiles.of("prod")) && secret.startsWith(DEV_SECRET_PREFIX)) {
            throw new IllegalStateException(
                    "Refusing to start with the development JWT secret in the prod profile. "
                            + "Set the JWT_SECRET environment variable to a long random value.");
        }
        this.key = Keys.hmacShaKeyFor(secretBytes);
    }

    public String generateToken(String username, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + EXPIRATION_MS);

        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}