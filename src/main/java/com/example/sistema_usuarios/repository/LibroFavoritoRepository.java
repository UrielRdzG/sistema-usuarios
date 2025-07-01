package com.example.sistema_usuarios.repository;

import com.example.sistema_usuarios.model.LibroFavorito;
import com.example.sistema_usuarios.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LibroFavoritoRepository extends JpaRepository<LibroFavorito, Long> {

    // Buscar todos los libros favoritos de un usuario ordenados por fecha (más recientes primero)
    List<LibroFavorito> findByUsuarioOrderByFechaAgregadoDesc(Usuario usuario);

    // Buscar libros favoritos por ID de usuario
    List<LibroFavorito> findByUsuarioIdOrderByFechaAgregadoDesc(Long usuarioId);

    // Buscar un libro favorito específico por usuario, título y autor
    Optional<LibroFavorito> findByUsuarioAndTituloAndAutor(Usuario usuario, String titulo, String autor);

    // Verificar si ya existe un libro favorito para un usuario
    boolean existsByUsuarioAndTituloAndAutor(Usuario usuario, String titulo, String autor);

    // Contar libros favoritos de un usuario
    long countByUsuario(Usuario usuario);

    // Contar libros favoritos por ID de usuario
    long countByUsuarioId(Long usuarioId);

    // Buscar libros favoritos por título que contenga el texto (case insensitive)
    @Query("SELECT lf FROM LibroFavorito lf WHERE lf.usuario = :usuario AND LOWER(lf.titulo) LIKE LOWER(CONCAT('%', :titulo, '%')) ORDER BY lf.fechaAgregado DESC")
    List<LibroFavorito> findByUsuarioAndTituloContainingIgnoreCase(@Param("usuario") Usuario usuario, @Param("titulo") String titulo);

    // Buscar libros favoritos por autor que contenga el texto (case insensitive)
    @Query("SELECT lf FROM LibroFavorito lf WHERE lf.usuario = :usuario AND LOWER(lf.autor) LIKE LOWER(CONCAT('%', :autor, '%')) ORDER BY lf.fechaAgregado DESC")
    List<LibroFavorito> findByUsuarioAndAutorContainingIgnoreCase(@Param("usuario") Usuario usuario, @Param("autor") String autor);

    // Eliminar libro favorito por usuario, título y autor
    void deleteByUsuarioAndTituloAndAutor(Usuario usuario, String titulo, String autor);

    // Eliminar todos los libros favoritos de un usuario
    void deleteByUsuario(Usuario usuario);
}