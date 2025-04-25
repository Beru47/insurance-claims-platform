package com.vermeg.sinistpro.security;

import java.security.SecureRandom;
import java.util.Base64;

public class KeyGenerator {
    public static void main(String[] args) {
        SecureRandom random = new SecureRandom();
        byte[] key = new byte[64]; // 64 bytes = 512 bits
        random.nextBytes(key);
        String base64Key = Base64.getEncoder().encodeToString(key);
        System.out.println("Generated key (base64): " + base64Key);
        System.out.println("Key length (bytes): " + Base64.getDecoder().decode(base64Key).length);
    }
}