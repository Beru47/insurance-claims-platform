package com.vermeg.sinistpro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.vermeg.sinistpro", "com.vermeg.sinistpro.security", "com.vermeg.sinistpro.config"})
public class SinistProApplication {

    public static void main(String[] args) {
        SpringApplication.run(SinistProApplication.class, args);
        System.out.println("✅ SinistPro Application is running!");
    }
}