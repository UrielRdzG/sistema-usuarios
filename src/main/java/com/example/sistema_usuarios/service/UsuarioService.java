package com.example.sistema_usuarios.service;

import com.example.sistema_usuarios.model.Usuario;
import com.example.sistema_usuarios.model.Rol;
import com.example.sistema_usuarios.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Crear nuevo usuario
    public Usuario crearUsuario(Usuario usuario) {
        // Encriptar contraseña
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        usuario.setFechaCreacion(LocalDateTime.now());
        usuario.setFechaActualizacion(LocalDateTime.now());
        usuario.setActivo(true);

        return usuarioRepository.save(usuario);
    }

    // Registrar nuevo usuario (rol por defecto: USUARIO)
    public Usuario registrarUsuario(Usuario usuario) {
        usuario.setRol(Rol.USUARIO);
        return crearUsuario(usuario);
    }

    // Actualizar usuario
    public Usuario actualizarUsuario(Usuario usuario) {
        usuario.setFechaActualizacion(LocalDateTime.now());
        return usuarioRepository.save(usuario);
    }

    // Actualizar perfil del usuario (sin cambiar rol)
    public Usuario actualizarPerfil(Usuario usuarioActualizado, String nuevaPassword) {
        Usuario usuarioExistente = obtenerUsuarioPorId(usuarioActualizado.getId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Actualizar campos básicos
        usuarioExistente.setNombre(usuarioActualizado.getNombre());
        usuarioExistente.setApellidoPaterno(usuarioActualizado.getApellidoPaterno());
        usuarioExistente.setApellidoMaterno(usuarioActualizado.getApellidoMaterno());
        usuarioExistente.setEmail(usuarioActualizado.getEmail());
        usuarioExistente.setNumeroTelefonico(usuarioActualizado.getNumeroTelefonico());
        usuarioExistente.setUsername(usuarioActualizado.getUsername()); // También agregar esta línea

        // Si se proporciona una nueva contraseña, encriptarla
        if (nuevaPassword != null && !nuevaPassword.trim().isEmpty()) {
            usuarioExistente.setPassword(passwordEncoder.encode(nuevaPassword));
        }

        // Actualizar foto de perfil si se proporciona
        if (usuarioActualizado.getFotoPerfil() != null) {
            usuarioExistente.setFotoPerfil(usuarioActualizado.getFotoPerfil());
        }

        return actualizarUsuario(usuarioExistente);
    }

    // Procesar y guardar foto de perfil
    public void guardarFotoPerfil(Long usuarioId, MultipartFile archivo) throws IOException {
        if (archivo != null && !archivo.isEmpty()) {
            Usuario usuario = obtenerUsuarioPorId(usuarioId)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            usuario.setFotoPerfil(archivo.getBytes());
            actualizarUsuario(usuario);
        }
    }

    // Eliminar usuario (soft delete)
    public void eliminarUsuario(Long id) {
        Usuario usuario = obtenerUsuarioPorId(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        usuario.setActivo(false);
        actualizarUsuario(usuario);
    }

    // Eliminar usuario permanentemente (solo para admin)
    public void eliminarUsuarioPermanentemente(Long id) {
        usuarioRepository.deleteById(id);
    }

    // Obtener usuario por ID
    public Optional<Usuario> obtenerUsuarioPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    // Obtener usuario por username
    public Optional<Usuario> obtenerUsuarioPorUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    // Obtener usuario por email
    public Optional<Usuario> obtenerUsuarioPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    // Listar todos los usuarios activos
    public List<Usuario> listarUsuariosActivos() {
        return usuarioRepository.findByActivoTrue();
    }

    // Listar todos los usuarios
    public List<Usuario> listarTodosLosUsuarios() {
        return usuarioRepository.findAll();
    }

    // Listar usuarios por rol
    public List<Usuario> listarUsuariosPorRol(Rol rol) {
        return usuarioRepository.findByRolAndActivoTrue(rol);
    }

    // Buscar usuarios por nombre
    public List<Usuario> buscarUsuariosPorNombre(String nombre) {
        return usuarioRepository.findByNombreCompletoContainingIgnoreCase(nombre);
    }

    // Verificar si existe username
    public boolean existeUsername(String username) {
        return usuarioRepository.existsByUsername(username);
    }

    // Verificar si existe email
    public boolean existeEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    // Verificar si existe username excluyendo el usuario actual
    public boolean existeUsernameExcluyendoUsuario(String username, Long usuarioId) {
        Optional<Usuario> usuario = usuarioRepository.findByUsername(username);
        return usuario.isPresent() && !usuario.get().getId().equals(usuarioId);
    }

    // Verificar si existe email excluyendo el usuario actual
    public boolean existeEmailExcluyendoUsuario(String email, Long usuarioId) {
        Optional<Usuario> usuario = usuarioRepository.findByEmail(email);
        return usuario.isPresent() && !usuario.get().getId().equals(usuarioId);
    }

    // Cambiar rol de usuario (solo para admin)
    public Usuario cambiarRol(Long usuarioId, Rol nuevoRol) {
        Usuario usuario = obtenerUsuarioPorId(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        usuario.setRol(nuevoRol);
        return actualizarUsuario(usuario);
    }

    // Activar/desactivar usuario
    public Usuario cambiarEstadoUsuario(Long usuarioId, boolean activo) {
        Usuario usuario = obtenerUsuarioPorId(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        usuario.setActivo(activo);
        return actualizarUsuario(usuario);
    }

    // Contar usuarios por rol
    public long contarUsuariosPorRol(Rol rol) {
        return usuarioRepository.countByRol(rol);
    }

    // Contar usuarios activos:
    public long contarUsuariosActivos() {
        return usuarioRepository.countByActivoTrue();
    }

}
