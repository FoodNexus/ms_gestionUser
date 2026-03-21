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
    // On met une phrase secrète assez longue (au moins 32 caractères pour HS256)
    private static final String SECRET = "CeciEstUneCleSecreteTresLonguePourLeFameuxProjetFoodNexus2026";
    private static final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());
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

    public String getEmailFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}