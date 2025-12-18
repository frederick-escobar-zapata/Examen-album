package ipss.cl.album.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtUtil jwtUtil;

    public SecurityConfig(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    		// Con esta configuración yo defino qué rutas protejo y cómo aplico JWT.
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) ->
                    response.sendRedirect("/auth/login"))
                .accessDeniedHandler((request, response, accessDeniedException) ->
                    response.sendRedirect("/auth/login"))
            )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**", "/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                    .requestMatchers("/api/**", "/vista/**").authenticated()
                        .anyRequest().permitAll()
                )
                .addFilterBefore(new JwtRequestFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

}
