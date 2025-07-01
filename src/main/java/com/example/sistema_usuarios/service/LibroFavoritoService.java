package com.example.sistema_usuarios.service;

import com.example.sistema_usuarios.model.LibroFavorito;
import com.example.sistema_usuarios.model.Usuario;
import com.example.sistema_usuarios.repository.LibroFavoritoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class LibroFavoritoService {

    private static final Logger logger = LoggerFactory.getLogger(LibroFavoritoService.class);

    @Autowired
    private LibroFavoritoRepository libroFavoritoRepository;

    @Autowired
    private UsuarioService usuarioService;

    /**
     * Agregar un libro a favoritos
     */
    public LibroFavorito agregarAFavoritos(Long usuarioId, String titulo, String autor,
                                           String añoPublicacion, String coverId, String temas) {
        logger.info("Agregando libro a favoritos para usuario ID: {}, Título: {}", usuarioId, titulo);

        Usuario usuario = usuarioService.obtenerUsuarioPorId(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + usuarioId));

        // Verificar si el libro ya está en favoritos
        if (esLibroFavorito(usuario, titulo, autor)) {
            throw new RuntimeException("El libro ya está en tus favoritos");
        }

        LibroFavorito libroFavorito = new LibroFavorito(usuario, titulo, autor, añoPublicacion, coverId, temas);
        LibroFavorito savedLibro = libroFavoritoRepository.save(libroFavorito);

        logger.info("Libro agregado a favoritos exitosamente. ID: {}", savedLibro.getId());
        return savedLibro;
    }

    /**
     * Eliminar un libro de favoritos
     */
    public void eliminarDeFavoritos(Long usuarioId, String titulo, String autor) {
        logger.info("Eliminando libro de favoritos para usuario ID: {}, Título: {}", usuarioId, titulo);

        Usuario usuario = usuarioService.obtenerUsuarioPorId(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + usuarioId));

        Optional<LibroFavorito> libroFavorito = libroFavoritoRepository
                .findByUsuarioAndTituloAndAutor(usuario, titulo, autor);

        if (libroFavorito.isPresent()) {
            libroFavoritoRepository.delete(libroFavorito.get());
            logger.info("Libro eliminado de favoritos exitosamente");
        } else {
            throw new RuntimeException("El libro no se encontró en tus favoritos");
        }
    }

    /**
     * Eliminar libro favorito por ID
     */
    public void eliminarLibroFavorito(Long libroFavoritoId, Long usuarioId) {
        logger.info("Eliminando libro favorito ID: {} para usuario ID: {}", libroFavoritoId, usuarioId);

        LibroFavorito libroFavorito = libroFavoritoRepository.findById(libroFavoritoId)
                .orElseThrow(() -> new RuntimeException("Libro favorito no encontrado"));

        // Verificar que el libro pertenece al usuario
        if (!libroFavorito.getUsuario().getId().equals(usuarioId)) {
            throw new RuntimeException("No tienes permiso para eliminar este libro");
        }

        libroFavoritoRepository.delete(libroFavorito);
        logger.info("Libro favorito eliminado exitosamente");
    }

    /**
     * Obtener todos los libros favoritos de un usuario
     */
    @Transactional(readOnly = true)
    public List<LibroFavorito> obtenerLibrosFavoritos(Long usuarioId) {
        logger.debug("Obteniendo libros favoritos para usuario ID: {}", usuarioId);
        return libroFavoritoRepository.findByUsuarioIdOrderByFechaAgregadoDesc(usuarioId);
    }

    /**
     * Obtener libros favoritos por objeto Usuario
     */
    @Transactional(readOnly = true)
    public List<LibroFavorito> obtenerLibrosFavoritos(Usuario usuario) {
        return libroFavoritoRepository.findByUsuarioOrderByFechaAgregadoDesc(usuario);
    }

    /**
     * Verificar si un libro está en favoritos
     */
    @Transactional(readOnly = true)
    public boolean esLibroFavorito(Usuario usuario, String titulo, String autor) {
        return libroFavoritoRepository.existsByUsuarioAndTituloAndAutor(usuario, titulo, autor);
    }

    /**
     * Verificar si un libro está en favoritos por ID de usuario
     */
    @Transactional(readOnly = true)
    public boolean esLibroFavorito(Long usuarioId, String titulo, String autor) {
        Usuario usuario = usuarioService.obtenerUsuarioPorId(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return esLibroFavorito(usuario, titulo, autor);
    }

    /**
     * Contar libros favoritos de un usuario
     */
    @Transactional(readOnly = true)
    public long contarLibrosFavoritos(Long usuarioId) {
        return libroFavoritoRepository.countByUsuarioId(usuarioId);
    }

    /**
     * Buscar libros favoritos por título
     */
    @Transactional(readOnly = true)
    public List<LibroFavorito> buscarPorTitulo(Usuario usuario, String titulo) {
        return libroFavoritoRepository.findByUsuarioAndTituloContainingIgnoreCase(usuario, titulo);
    }

    /**
     * Buscar libros favoritos por autor
     */
    @Transactional(readOnly = true)
    public List<LibroFavorito> buscarPorAutor(Usuario usuario, String autor) {
        return libroFavoritoRepository.findByUsuarioAndAutorContainingIgnoreCase(usuario, autor);
    }

    /**
     * Obtener libro favorito por ID
     */
    @Transactional(readOnly = true)
    public Optional<LibroFavorito> obtenerLibroFavorito(Long id) {
        return libroFavoritoRepository.findById(id);
    }

    /**
     * Eliminar todos los libros favoritos de un usuario
     */
    public void eliminarTodosLosLibrosFavoritos(Usuario usuario) {
        logger.info("Eliminando todos los libros favoritos para usuario: {}", usuario.getUsername());
        libroFavoritoRepository.deleteByUsuario(usuario);
    }
}