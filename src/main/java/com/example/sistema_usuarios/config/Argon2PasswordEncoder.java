package com.example.sistema_usuarios.config;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import org.springframework.security.crypto.password.PasswordEncoder;

public class Argon2PasswordEncoder implements PasswordEncoder {

    private final Argon2 argon2;

    public Argon2PasswordEncoder() {
        // Crear una instancia de Argon2 con configuración por defecto
        this.argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);
    }

    @Override
    public String encode(CharSequence rawPassword) {
        try {
            // Hashear la contraseña con Argon2
            // Parámetros: iterations=2, memory=65536, parallelism=1
            return argon2.hash(2, 65536, 1, rawPassword.toString().toCharArray());
        } catch (Exception e) {
            throw new RuntimeException("Error al encriptar la contraseña", e);
        }
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        try {
            // Verificar si la contraseña coincide con el hash
            return argon2.verify(encodedPassword, rawPassword.toString().toCharArray());
        } catch (Exception e) {
            return false;
        } finally {
            // Limpiar la memoria por seguridad
            argon2.wipeArray(rawPassword.toString().toCharArray());
        }
    }
}