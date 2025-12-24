package ipss.cl.album.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtRequestFilter extends OncePerRequestFilter{

    private static final Logger logger = LoggerFactory.getLogger(JwtRequestFilter.class);

    private final JwtUtil jwtUtil;

    public JwtRequestFilter(JwtUtil jwtUtil){
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                    HttpServletResponse response,
                    FilterChain chain) throws ServletException, IOException{

        final String authHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        // Verifico si el header de autorización existe y comienza con "Bearer".
        if(authHeader != null && authHeader.startsWith("Bearer ")){
            jwt = authHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwt);
            } catch (ExpiredJwtException eje) {
                logger.info("JWT expired: {}", eje.getMessage());
                // no autenticamos; si el token venía en cookie, limpiarla
                clearJwtCookie(response);
            } catch (JwtException je) {
                logger.debug("Invalid JWT in Authorization header: {}", je.getMessage());
            }
        } else {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("jwtToken".equals(cookie.getName())) {
                        jwt = cookie.getValue();
                        if (jwt != null && !jwt.isEmpty()) {
                            try {
                                username = jwtUtil.extractUsername(jwt);
                            } catch (ExpiredJwtException eje) {
                                logger.info("JWT cookie expired: {}", eje.getMessage());
                                clearJwtCookie(response);
                            } catch (JwtException je) {
                                logger.debug("Invalid JWT cookie: {}", je.getMessage());
                            }
                        }
                        break;
                    }
                }
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(username, null, java.util.Collections.emptyList());
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        chain.doFilter(request, response);

    }

    private void clearJwtCookie(HttpServletResponse response) {
        Cookie empty = new Cookie("jwtToken", "");
        empty.setPath("/");
        empty.setHttpOnly(true);
        empty.setMaxAge(0);
        response.addCookie(empty);
    }

}
