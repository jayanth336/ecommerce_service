package com.org.ecommerce.common.security;

import com.org.ecommerce.common.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    private SecretKey getSigningKey() {
        // WE ARE USING HS256 HERE. IT IS DECIDED PURELY BASED ON THE SECRET KEY LENGTH.
        // HERE THE KEY LENGTH IS 47 CHARS = 47 BYTES
        // HS256 NEEDS BYTES >= 32
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(String email, Role role) {
        return Jwts.builder()
                .subject(email) // WHO IS USER OF THIS TOKEN
                .claim("role", role.toString()) // EXTRA DATA ATTACHED TO PAYLOAD
                .issuedAt(new Date()) // CREATION TIME - NEW DATE() GIVES THE COMPLETE TIME INCLUDING MILLISECONDS
                .expiration(new Date(System.currentTimeMillis() + expiration)) // THAT'S WHY HERE ALSO WE ARE TAKING TIME IN MILLIS
                .signWith(getSigningKey()) // Signs in with the secret key
                .compact(); // Get the final JWT string
    }

    // Extract email
    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    // Check expiry
    public boolean isTokenExpired(String token) {
        return extractClaims(token)
                .getExpiration()
                .before(new Date(System.currentTimeMillis()));
    }

    // Validate token
    public boolean isTokenValid(String token, UserDetails userDetails) {
        String email = extractEmail(token);
        return email.equals(userDetails.getUsername())
                && (isTokenExpired(token) == false);
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
