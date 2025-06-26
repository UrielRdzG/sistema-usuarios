package com.example.sistema_usuarios.service;


import com.example.sistema_usuarios.model.LibroRecomendacion;
import com.example.sistema_usuarios.model.Usuario;
import com.example.sistema_usuarios.repository.LibroRecomendacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class LibroRecomendacionService {

    @Autowired
    private LibroRecomendacionRepository libroRecomendacionRepository;

    // Guardar una nueva recomendación
    public LibroRecomendacion guardarRecomendacion(LibroRecomendacion libroRecomendacion) {
        return libroRecomendacionRepository.save(libroRecomendacion);
    }

    // Crear y guardar recomendación desde datos de la API
    public LibroRecomendacion crearRecomendacionDesdeAPI(
            String titulo,
            String autor,
            Integer añoPublicacion,
            String coverId,
            String openLibraryKey,
            String temas,
            Usuario usuario) {

        // Verificar si ya existe
        if (openLibraryKey != null && existeRecomendacion(usuario, openLibraryKey)) {
            throw new RuntimeException("Este libro ya está en tu lista de recomendaciones");
        }

        LibroRecomendacion recomendacion = new LibroRecomendacion();
        recomendacion.setTitulo(titulo);
        recomendacion.setAutor(autor);
        recomendacion.setAñoPublicacion(añoPublicacion);
        recomendacion.setCoverId(coverId);
        recomendacion.setOpenLibraryKey(openLibraryKey);
        recomendacion.setTemas(temas);
        recomendacion.setUsuario(usuario);

        return guardarRecomendacion(recomendacion);
    }

    // Obtener todas las recomendaciones de un usuario
    public List<LibroRecomendacion> obtenerRecomendacionesPorUsuario(Usuario usuario) {
        return libroRecomendacionRepository.findByUsuarioOrderByFechaGuardadoDesc(usuario);
    }

    // Obtener recomendaciones por estado de lectura
    public List<LibroRecomendacion> obtenerRecomendacionesPorEstado(
            Usuario usuario, LibroRecomendacion.EstadoLectura estado) {
        return libroRecomendacionRepository.findByUsuarioAndEstadoLecturaOrderByFechaGuardadoDesc(usuario, estado);
    }

    // Verificar si existe una recomendación
    public boolean existeRecomendacion(Usuario usuario, String openLibraryKey) {
        return libroRecomendacionRepository.existsByUsuarioAndOpenLibraryKey(usuario, openLibraryKey);
    }

    // Obtener recomendación por ID
    public Optional<LibroRecomendacion> obtenerPorId(Long id) {
        return libroRecomendacionRepository.findById(id);
    }

    // Actualizar estado de lectura
    public LibroRecomendacion actualizarEstadoLectura(Long id, LibroRecomendacion.EstadoLectura nuevoEstado) {
        Optional<LibroRecomendacion> recomendacionOpt = obtenerPorId(id);
        if (recomendacionOpt.isPresent()) {
            LibroRecomendacion recomendacion = recomendacionOpt.get();
            recomendacion.setEstadoLectura(nuevoEstado);
            return guardarRecomendacion(recomendacion);
        }
        throw new RuntimeException("Recomendación no encontrada");
    }

    // Actualizar notas personales
    public LibroRecomendacion actualizarNotas(Long id, String notas) {
        Optional<LibroRecomendacion> recomendacionOpt = obtenerPorId(id);
        if (recomendacionOpt.isPresent()) {
            LibroRecomendacion recomendacion = recomendacionOpt.get();
            recomendacion.setNotasPersonales(notas);
            return guardarRecomendacion(recomendacion);
        }
        throw new RuntimeException("Recomendación no encontrada");
    }

    // Eliminar recomendación
    public void eliminarRecomendacion(Long id) {
        libroRecomendacionRepository.deleteById(id);
    }

    // Buscar por título
    public List<LibroRecomendacion> buscarPorTitulo(Usuario usuario, String titulo) {
        return libroRecomendacionRepository.findByUsuarioAndTituloContainingIgnoreCase(usuario, titulo);
    }

    // Buscar por autor
    public List<LibroRecomendacion> buscarPorAutor(Usuario usuario, String autor) {
        return libroRecomendacionRepository.findByUsuarioAndAutorContainingIgnoreCase(usuario, autor);
    }

    // Obtener estadísticas del usuario
    public EstadisticasLibros obtenerEstadisticas(Usuario usuario) {
        EstadisticasLibros stats = new EstadisticasLibros();
        stats.setTotalLibros(libroRecomendacionRepository.countByUsuario(usuario));
        stats.setQuieroLeer(libroRecomendacionRepository.countByUsuarioAndEstadoLectura(
                usuario, LibroRecomendacion.EstadoLectura.QUIERO_LEER));
        stats.setLeyendo(libroRecomendacionRepository.countByUsuarioAndEstadoLectura(
                usuario, LibroRecomendacion.EstadoLectura.LEYENDO));
        stats.setLeidos(libroRecomendacionRepository.countByUsuarioAndEstadoLectura(
                usuario, LibroRecomendacion.EstadoLectura.LEIDO));
        return stats;
    }

    // Obtener últimas recomendaciones
    public List<LibroRecomendacion> obtenerUltimasRecomendaciones(Usuario usuario) {
        return libroRecomendacionRepository.findTop5ByUsuarioOrderByFechaGuardadoDesc(usuario);
    }

    // Clase para estadísticas
    public static class EstadisticasLibros {
        private long totalLibros;
        private long quieroLeer;
        private long leyendo;
        private long leidos;

        // Getters y setters
        public long getTotalLibros() { return totalLibros; }
        public void setTotalLibros(long totalLibros) { this.totalLibros = totalLibros; }

        public long getQuieroLeer() { return quieroLeer; }
        public void setQuieroLeer(long quieroLeer) { this.quieroLeer = quieroLeer; }

        public long getLeyendo() { return leyendo; }
        public void setLeyendo(long leyendo) { this.leyendo = leyendo; }

        public long getLeidos() { return leidos; }
        public void setLeidos(long leidos) { this.leidos = leidos; }
    }
}
