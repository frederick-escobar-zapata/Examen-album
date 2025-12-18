package ipss.cl.album.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtRequestFilter extends OncePerRequestFilter{

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


        // Aquí yo verifico si el header de autorización existe y comienza con "Bearer".
        if(authHeader != null && authHeader.startsWith("Bearer ")){
            jwt = authHeader.substring(7);
            username = jwtUtil.extractUsername(jwt);
        } else {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("jwtToken".equals(cookie.getName())) {
                        jwt = cookie.getValue();
                        if (jwt != null && !jwt.isEmpty()) {
                            username = jwtUtil.extractUsername(jwt);
                        }
                        break;
                    }
                }
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // En este punto yo creo el objeto de autenticación y lo asocio al contexto de seguridad.
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(username, null, java.util.Collections.emptyList());
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);

        }

        chain.doFilter(request, response);

    }

    

}
