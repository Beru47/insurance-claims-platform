package com.vermeg.sinistpro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication  // Enables Spring Boot auto-configuration and component scanning
public class SinistProApplication {

    public static void main(String[] args) {
        SpringApplication.run(SinistProApplication.class, args);  // Bootstraps the Spring Boot application
        System.out.println("✅ SinistPro Application is running!");
    }
}
