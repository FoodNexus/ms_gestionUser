package tn.esprit.ms_gestionuser.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {
    // Une clé secrète pour signer le token (en prod, on la met dans application.properties)
    private static final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private static final int jwtExpirationMs = 86400000; // 24 heures

    public String generateToken(String email, String role) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role) // On cache le rôle dans le token !
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key)
                .compact();
    }
}
