package com.vermeg.sinistpro.config;

import com.vermeg.sinistpro.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

public class JwtConfigurer extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

    private static final Logger logger = LoggerFactory.getLogger(JwtConfigurer.class);
    private final JwtUtil jwtUtil;

    public JwtConfigurer(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
        logger.info("JwtConfigurer initialized");
    }

    @Override
    public void configure(HttpSecurity http) {
        JwtFilter customFilter = new JwtFilter(jwtUtil);
        logger.info("Applying JwtFilter to security chain");
        http.addFilterBefore(customFilter, UsernamePasswordAuthenticationFilter.class);
    }
}