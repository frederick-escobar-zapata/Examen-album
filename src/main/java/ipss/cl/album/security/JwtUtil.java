package ipss.cl.album.security;

import java.security.Key;
import java.util.Date;

import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.ExpiredJwtException;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    // Con esta clave privada yo firmo los tokens JWT.
    private final Key SECRET_KEY = Keys.hmacShaKeyFor("claveSecretaSuperSegura1234567890".getBytes());

    // Con este método yo genero un token JWT para un nombre de usuario.
    public String generateToken(String username){
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    // Con este método yo extraigo el nombre de usuario contenido en un token.
    public String extractUsername(String token){
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (ExpiredJwtException eje) {
            logger.info("JWT expired when extracting username: {}", eje.getMessage());
            return null;
        } catch (JwtException je) {
            logger.debug("Invalid JWT when extracting username: {}", je.getMessage());
            return null;
        }
    }


}
