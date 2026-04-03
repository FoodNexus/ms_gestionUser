package tn.esprit.ms_gestionuser.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // On désactive le CSRF (car API REST Stateless)
                .csrf(csrf -> csrf.disable())

                // On exige une authentification pour TOUTES les requêtes
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()
                )

                // On indique que ce microservice lit les JWT fournis par Keycloak via la Gateway
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(org.springframework.security.config.Customizer.withDefaults())
                );

        return http.build();
    }
}