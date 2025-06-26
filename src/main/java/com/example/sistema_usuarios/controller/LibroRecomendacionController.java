package com.example.sistema_usuarios.controller;

import com.example.sistema_usuarios.model.LibroRecomendacion;
import com.example.sistema_usuarios.model.Usuario;
import com.example.sistema_usuarios.service.LibroRecomendacionService;
import com.example.sistema_usuarios.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/libros")
@CrossOrigin(origins = "*")
public class LibroRecomendacionController {

    @Autowired
    private LibroRecomendacionService libroRecomendacionService;

    @Autowired
    private UsuarioService usuarioService;

    // Guardar nueva recomendación
    @PostMapping("/guardar")
    public ResponseEntity<?> guardarRecomendacion(
            @RequestBody LibroRecomendacionRequest request,
            Authentication authentication) {

        try {
            Usuario usuario = usuarioService.buscarPorUsername(authentication.getName());

            LibroRecomendacion recomendacion = libroRecomendacionService.crearRecomendacionDesdeAPI(
                    request.getTitulo(),
                    request.getAutor(),
                    request.getAñoPublicacion(),
                    request.getCoverId(),
                    request.getOpenLibraryKey(),
                    request.getTemas(),
                    usuario
            );

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Libro guardado exitosamente",
                    "libro", recomendacion
            ));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Error interno del servidor"
            ));
        }
    }

    // Obtener todas las recomendaciones del usuario
    @GetMapping("/mis-libros")
    public ResponseEntity<List<LibroRecomendacion>> obtenerMisLibros(Authentication authentication) {
        Usuario usuario = usuarioService.buscarPorUsername(authentication.getName());
        List<LibroRecomendacion> recomendaciones = libroRecomendacionService.obtenerRecomendacionesPorUsuario(usuario);
        return ResponseEntity.ok(recomendaciones);
    }

    // Obtener libros por estado
    @GetMapping("/por-estado/{estado}")
    public ResponseEntity<List<LibroRecomendacion>> obtenerLibrosPorEstado(
            @PathVariable String estado,
            Authentication authentication) {

        Usuario usuario = usuarioService.buscarPorUsername(authentication.getName());
        LibroRecomendacion.EstadoLectura estadoLectura = LibroRecomendacion.EstadoLectura.valueOf(estado.toUpperCase());
        List<LibroRecomendacion> recomendaciones = libroRecomendacionService.obtenerRecomendacionesPorEstado(usuario, estadoLectura);
        return ResponseEntity.ok(recomendaciones);
    }

    // Actualizar estado de lectura
    @PutMapping("/{id}/estado")
    public ResponseEntity<?> actualizarEstado(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            Authentication authentication) {

        try {
            String nuevoEstado = request.get("estado");
            LibroRecomendacion.EstadoLectura estadoLectura = LibroRecomendacion.EstadoLectura.valueOf(nuevoEstado.toUpperCase());

            LibroRecomendacion recomendacion = libroRecomendacionService.actualizarEstadoLectura(id, estadoLectura);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Estado actualizado exitosamente",
                    "libro", recomendacion
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Error al actualizar el estado"
            ));
        }
    }

    // Actualizar notas personales
    @PutMapping("/{id}/notas")
    public ResponseEntity<?> actualizarNotas(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {

        try {
            String notas = request.get("notas");
            LibroRecomendacion recomendacion = libroRecomendacionService.actualizarNotas(id, notas);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Notas actualizadas exitosamente",
                    "libro", recomendacion
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Error al actualizar las notas"
            ));
        }
    }

    // Eliminar recomendación
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarRecomendacion(@PathVariable Long id) {
        try {
            libroRecomendacionService.eliminarRecomendacion(id);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Libro eliminado exitosamente"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Error al eliminar el libro"
            ));
        }
    }

    // Verificar si un libro ya está guardado
    @GetMapping("/verificar/{openLibraryKey}")
    public ResponseEntity<Map<String, Boolean>> verificarLibro(
            @PathVariable String openLibraryKey,
            Authentication authentication) {

        Usuario usuario = usuarioService.buscarPorUsername(authentication.getName());
        boolean existe = libroRecomendacionService.existeRecomendacion(usuario, openLibraryKey);

        return ResponseEntity.ok(Map.of("existe", existe));
    }

    // Obtener estadísticas
    @GetMapping("/estadisticas")
    public ResponseEntity<LibroRecomendacionService.EstadisticasLibros> obtenerEstadisticas(
            Authentication authentication) {

        Usuario usuario = usuarioService.buscarPorUsername(authentication.getName());
        LibroRecomendacionService.EstadisticasLibros stats = libroRecomendacionService.obtenerEstadisticas(usuario);

        return ResponseEntity.ok(stats);
    }

    // Obtener últimas recomendaciones
    @GetMapping("/recientes")
    public ResponseEntity<List<LibroRecomendacion>> obtenerRecientes(Authentication authentication) {
        Usuario usuario = usuarioService.buscarPorUsername(authentication.getName());
        List<LibroRecomendacion> recientes = libroRecomendacionService.obtenerUltimasRecomendaciones(usuario);
        return ResponseEntity.ok(recientes);
    }

    // Clase para el request de guardar libro
    public static class LibroRecomendacionRequest {
        private String titulo;
        private String autor;
        private Integer añoPublicacion;
        private String coverId;
        private String openLibraryKey;
        private String temas;

        // Getters y setters
        public String getTitulo() { return titulo; }
        public void setTitulo(String titulo) { this.titulo = titulo; }

        public String getAutor() { return autor; }
        public void setAutor(String autor) { this.autor = autor; }

        public Integer getAñoPublicacion() { return añoPublicacion; }
        public void setAñoPublicacion(Integer añoPublicacion) { this.añoPublicacion = añoPublicacion; }

        public String getCoverId() { return coverId; }
        public void setCoverId(String coverId) { this.coverId = coverId; }

        public String getOpenLibraryKey() { return openLibraryKey; }
        public void setOpenLibraryKey(String openLibraryKey) { this.openLibraryKey = openLibraryKey; }

        public String getTemas() { return temas; }
        public void setTemas(String temas) { this.temas = temas; }
    }
}