package com.renewsim.backend;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BCryptGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // Cambia esta contraseña por la que quieras usar
        String rawPassword = "admin123";

        // Genera el hash
        String encodedPassword = encoder.encode(rawPassword);

        System.out.println("Contraseña en texto plano: " + rawPassword);
        System.out.println("Hash BCrypt generado: " + encodedPassword);
    }
}
