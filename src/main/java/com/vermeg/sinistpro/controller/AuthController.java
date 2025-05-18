package com.vermeg.sinistpro.controller;

import com.vermeg.sinistpro.model.User;
import com.vermeg.sinistpro.security.JwtUtil;
import com.vermeg.sinistpro.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    public AuthController(UserService userService, AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/signup")
    public ResponseEntity<User> signup(@Valid @RequestBody SignupRequest signupRequest) {
        User user = new User(signupRequest.getUsername(), signupRequest.getPassword(), signupRequest.getEmail());
        User savedUser = userService.signup(user, "ROLE_CLIENT", signupRequest);
        logger.info("User signed up: {}", savedUser.getUsername());
        return ResponseEntity.ok(savedUser);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        User user = userService.findUserForLogin(loginRequest.getIdentifier())
                .orElseThrow(() -> new IllegalArgumentException("User not found with provided identifier"));

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), loginRequest.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String role = authentication.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .findFirst()
                .orElse("ROLE_USER");
        String token = jwtUtil.generateToken(String.valueOf(user.getId()), role);
        logger.info("Generated JWT token for user {}: {}", user.getUsername(), token);
        System.out.println("Generated JWT token: " + token);

        return ResponseEntity.ok(new LoginResponse(token));
    }

    @PostMapping("/admin-login")
    public ResponseEntity<?> adminLogin(@RequestBody LoginRequest loginRequest) {
        User user = userService.findUserForLogin(loginRequest.getIdentifier())
                .orElseThrow(() -> new IllegalArgumentException("User not found with provided identifier"));

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), loginRequest.getPassword())
        );

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            return ResponseEntity.status(403).body("Access denied: This login is for admins only.");
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String role = authentication.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .findFirst()
                .orElse("ROLE_ADMIN");
        String token = jwtUtil.generateToken(String.valueOf(user.getId()), role);
        logger.info("Generated JWT token for admin {}: {}", user.getUsername(), token);
        return ResponseEntity.ok(new LoginResponse(token));
    }

    @PostMapping("/expert-login")
    public ResponseEntity<?> expertLogin(@RequestBody LoginRequest loginRequest) {
        User user = userService.findUserForLogin(loginRequest.getIdentifier())
                .orElseThrow(() -> new IllegalArgumentException("User not found with provided identifier"));

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), loginRequest.getPassword())
        );

        boolean isExpert = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_EXPERT"));
        if (!isExpert) {
            return ResponseEntity.status(403).body("Access denied: This login is for experts only.");
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String role = authentication.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .findFirst()
                .orElse("ROLE_EXPERT");
        String token = jwtUtil.generateToken(String.valueOf(user.getId()), role);
        logger.info("Generated JWT token for expert {}: {}", user.getUsername(), token);
        return ResponseEntity.ok(new LoginResponse(token));
    }

}

