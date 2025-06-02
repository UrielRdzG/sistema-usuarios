package com.example.sistema_usuarios.repository;

import com.example.sistema_usuarios.model.Usuario;
import com.example.sistema_usuarios.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Buscar usuario por username
    Optional<Usuario> findByUsername(String username);

    // Buscar usuario por email
    Optional<Usuario> findByEmail(String email);

    // Verificar si existe un usuario con el username dado
    boolean existsByUsername(String username);

    // Verificar si existe un usuario con el email dado
    boolean existsByEmail(String email);

    // Buscar usuarios activos
    List<Usuario> findByActivoTrue();

    // Buscar usuarios por rol
    List<Usuario> findByRol(Rol rol);

    // Buscar usuarios activos por rol
    List<Usuario> findByRolAndActivoTrue(Rol rol);

    // Buscar usuario por username y que esté activo
    @Query("SELECT u FROM Usuario u WHERE u.username = :username AND u.activo = true")
    Optional<Usuario> findByUsernameAndActivoTrue(@Param("username") String username);

    // Buscar usuarios por nombre que contenga el texto dado (case insensitive)
    @Query("SELECT u FROM Usuario u WHERE LOWER(CONCAT(u.nombre, ' ', u.apellidoPaterno, ' ', u.apellidoMaterno)) LIKE LOWER(CONCAT('%', :nombre, '%')) AND u.activo = true")
    List<Usuario> findByNombreCompletoContainingIgnoreCase(@Param("nombre") String nombre);

    // Contar usuarios por rol
    long countByRol(Rol rol);

    // Contar usuarios activos
    long countByActivoTrue();
}
