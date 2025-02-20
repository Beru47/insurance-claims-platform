package com.vermeg.sinistpro.config;

import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class KeycloakConfig {

    // Keycloak config resolver for Spring Boot to fetch configurations
    @Bean
    public KeycloakSpringBootConfigResolver keycloakConfigResolver() {
        return new KeycloakSpringBootConfigResolver();
    }

    // Register session authentication strategy
    @Bean
    public SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new RegisterSessionAuthenticationStrategy(new SessionRegistryImpl());
    }

    // Security filter chain using HttpSecurity
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()  // Disable CSRF (for API security)
                .authorizeRequests()
                .antMatchers("/api/claims/**").hasRole("USER") // Only users with ROLE_USER can access this endpoint
                .antMatchers("/api/admin/**").hasRole("ADMIN") // Only users with ROLE_ADMIN can access this endpoint
                .anyRequest().authenticated() // Other endpoints need authentication
                .and()
                .oauth2Login()  // Keycloak OAuth2 login
                .and()
                .logout().logoutSuccessUrl("/"); // Redirect after logout

        return http.build();
    }
}
