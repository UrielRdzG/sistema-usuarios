package com.example.sistema_usuarios.security;


import com.example.sistema_usuarios.model.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {

    private final Usuario usuario;

    public CustomUserDetails(Usuario usuario) {
        this.usuario = usuario;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(
                new SimpleGrantedAuthority(usuario.getRol().getAuthority())
        );
    }

    @Override
    public String getPassword() {
        return usuario.getPassword();
    }

    @Override
    public String getUsername() {
        return usuario.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return usuario.isActivo();
    }

    // Metodo para obtener el usuario completo
    public Usuario getUsuario() {
        return usuario;
    }

    // Métodos de conveniencia
    public Long getId() {
        return usuario.getId();
    }

    public String getNombreCompleto() {
        return usuario.getNombreCompleto();
    }

    public String getEmail() {
        return usuario.getEmail();
    }

    public boolean isAdmin() {
        return usuario.getRol().name().equals("ADMINISTRADOR");
    }
}