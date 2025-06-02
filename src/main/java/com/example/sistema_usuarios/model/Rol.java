package com.example.sistema_usuarios.model;

public enum Rol {
    USUARIO("Usuario"),
    ADMINISTRADOR("Administrador");

    private final String displayName;

    Rol(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getAuthority() {
        return "ROLE_" + this.name();
    }
}
