package com.vermeg.sinistpro.service;


import com.vermeg.sinistpro.controller.SignupRequest;
import com.vermeg.sinistpro.model.*;
import com.vermeg.sinistpro.repository.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ClientRepository clientRepository;
    private final AdminRepository adminRepository;
    private final ExpertRepository expertRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository,
                       ClientRepository clientRepository, AdminRepository adminRepository,
                       ExpertRepository expertRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.clientRepository = clientRepository;
        this.adminRepository = adminRepository;
        this.expertRepository = expertRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User signup(User user, String roleName, SignupRequest signupRequest) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Encode the password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Find or create the role
        Role role = roleRepository.findByName(roleName)
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName(roleName);
                    return roleRepository.save(newRole);
                });
        user.addRole(role);

        // Save the user
        User savedUser = userRepository.save(user);

        // Create associated entity based on role
        if (roleName.equals("ROLE_CLIENT")) {
            if (signupRequest.getCin() == null) {
                throw new IllegalArgumentException("CIN is required for client signup");
            }

            Client client = new Client();
            client.setUser(savedUser);
            client.setCin(signupRequest.getCin());
            client.setNom(signupRequest.getNom());
            client.setPrenom(signupRequest.getPrenom());
            if (signupRequest.getDateNaissance() != null) {
                client.setDateNaissance(LocalDate.parse(signupRequest.getDateNaissance(), DateTimeFormatter.ISO_LOCAL_DATE));
            }
            client.setEmail(signupRequest.getEmail());
            client.setTelephone(signupRequest.getTelephone());
            client.setAdresse(signupRequest.getAdresse());
            client.setNationalite(signupRequest.getNationalite());
            client.setSexe(signupRequest.getSexe());
            clientRepository.save(client);
        } else if (roleName.equals("ROLE_ADMIN")) {
            Admin admin = new Admin();
            admin.setUser(savedUser);
            adminRepository.save(admin);
        } else if (roleName.equals("ROLE_EXPERT")) {
            Expert expert = new Expert();
            expert.setUser(savedUser);
            expertRepository.save(expert);
        }

        return savedUser;
    }

    public Optional<User> findUserForLogin(String identifier) {
        // First, try to find by email
        Optional<User> userByEmail = userRepository.findByEmail(identifier);
        if (userByEmail.isPresent()) {
            return userByEmail;
        }

        // If not found, try to find by CIN or telephone via the Client entity
        Optional<Client> client = clientRepository.findByCin(identifier);
        if (client.isPresent()) {
            return Optional.of(client.get().getUser());
        }

        client = clientRepository.findByTelephone(identifier);
        if (client.isPresent()) {
            return Optional.of(client.get().getUser());
        }

        return Optional.empty();
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found with username: " + username));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.getName()))
                        .collect(Collectors.toList())
        );
    }
}












/*
@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ClientRepository clientRepository;
    private final AdminRepository adminRepository;
    private final ExpertRepository expertRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository,
                       ClientRepository clientRepository, AdminRepository adminRepository,
                       ExpertRepository expertRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.clientRepository = clientRepository;
        this.adminRepository = adminRepository;
        this.expertRepository = expertRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User signup(User user, String roleName) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Encode the password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Find or create the role
        Role role = roleRepository.findByName(roleName)
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName(roleName);
                    return roleRepository.save(newRole);
                });
        user.addRole(role);

        // Save the user
        User savedUser = userRepository.save(user);

        // Create associated entity based on role
        if (roleName.equals("ROLE_CLIENT")) {
            Client client = new Client();
            client.setUser(savedUser);
            clientRepository.save(client);
        } else if (roleName.equals("ROLE_ADMIN")) {
            Admin admin = new Admin();
            admin.setUser(savedUser);
            adminRepository.save(admin);
        } else if (roleName.equals("ROLE_EXPERT")) {
            Expert expert = new Expert();
            expert.setUser(savedUser);
            expertRepository.save(expert);
        }

        return savedUser;
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found with username: " + username));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.getName()))
                        .collect(Collectors.toList())
        );
    }
}

 */