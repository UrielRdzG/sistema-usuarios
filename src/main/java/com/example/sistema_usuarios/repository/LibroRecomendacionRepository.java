package com.example.sistema_usuarios.repository;


import com.example.sistema_usuarios.model.LibroRecomendacion;
import com.example.sistema_usuarios.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LibroRecomendacionRepository extends JpaRepository<LibroRecomendacion, Long> {

    // Buscar todas las recomendaciones de un usuario
    List<LibroRecomendacion> findByUsuarioOrderByFechaGuardadoDesc(Usuario usuario);

    // Buscar por usuario y estado de lectura
    List<LibroRecomendacion> findByUsuarioAndEstadoLecturaOrderByFechaGuardadoDesc(
            Usuario usuario, LibroRecomendacion.EstadoLectura estadoLectura);

    // Verificar si un libro ya está guardado por el usuario
    boolean existsByUsuarioAndOpenLibraryKey(Usuario usuario, String openLibraryKey);

    // Buscar por usuario y clave de Open Library
    Optional<LibroRecomendacion> findByUsuarioAndOpenLibraryKey(Usuario usuario, String openLibraryKey);

    // Buscar libros por título (búsqueda parcial)
    @Query("SELECT lr FROM LibroRecomendacion lr WHERE lr.usuario = :usuario AND " +
            "LOWER(lr.titulo) LIKE LOWER(CONCAT('%', :titulo, '%')) ORDER BY lr.fechaGuardado DESC")
    List<LibroRecomendacion> findByUsuarioAndTituloContainingIgnoreCase(
            @Param("usuario") Usuario usuario, @Param("titulo") String titulo);

    // Buscar libros por autor
    @Query("SELECT lr FROM LibroRecomendacion lr WHERE lr.usuario = :usuario AND " +
            "LOWER(lr.autor) LIKE LOWER(CONCAT('%', :autor, '%')) ORDER BY lr.fechaGuardado DESC")
    List<LibroRecomendacion> findByUsuarioAndAutorContainingIgnoreCase(
            @Param("usuario") Usuario usuario, @Param("autor") String autor);

    // Contar libros por usuario
    long countByUsuario(Usuario usuario);

    // Contar libros por usuario y estado
    long countByUsuarioAndEstadoLectura(Usuario usuario, LibroRecomendacion.EstadoLectura estadoLectura);

    // Obtener los últimos libros guardados
    List<LibroRecomendacion> findTop5ByUsuarioOrderByFechaGuardadoDesc(Usuario usuario);

    // Buscar libros por temas
    @Query("SELECT lr FROM LibroRecomendacion lr WHERE lr.usuario = :usuario AND " +
            "LOWER(lr.temas) LIKE LOWER(CONCAT('%', :tema, '%')) ORDER BY lr.fechaGuardado DESC")
    List<LibroRecomendacion> findByUsuarioAndTemasContainingIgnoreCase(
            @Param("usuario") Usuario usuario, @Param("tema") String tema);
}