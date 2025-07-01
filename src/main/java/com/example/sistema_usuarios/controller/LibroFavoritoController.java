package com.example.sistema_usuarios.controller;

import com.example.sistema_usuarios.model.LibroFavorito;
import com.example.sistema_usuarios.security.CustomUserDetails;
import com.example.sistema_usuarios.service.LibroFavoritoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/libros-favoritos")
public class LibroFavoritoController {

    private static final Logger logger = LoggerFactory.getLogger(LibroFavoritoController.class);

    @Autowired
    private LibroFavoritoService libroFavoritoService;

    /**
     * Agregar libro a favoritos (AJAX)
     */
    @PostMapping("/agregar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> agregarAFavoritos(
            @RequestParam String titulo,
            @RequestParam(required = false) String autor,
            @RequestParam(required = false) String añoPublicacion,
            @RequestParam(required = false) String coverId,
            @RequestParam(required = false) String temas,
            Authentication authentication) {

        Map<String, Object> response = new HashMap<>();

        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Long usuarioId = userDetails.getId();

            logger.info("Intentando agregar libro a favoritos: {} por usuario: {}", titulo, usuarioId);

            LibroFavorito libroFavorito = libroFavoritoService.agregarAFavoritos(
                    usuarioId, titulo, autor, añoPublicacion, coverId, temas);

            response.put("success", true);
            response.put("message", "Libro agregado a favoritos exitosamente");
            response.put("libroId", libroFavorito.getId());

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            logger.error("Error al agregar libro a favoritos: {}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            logger.error("Error inesperado al agregar libro a favoritos", e);
            response.put("success", false);
            response.put("message", "Error interno del servidor");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Eliminar libro de favoritos (AJAX)
     */
    @DeleteMapping("/eliminar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> eliminarDeFavoritos(
            @RequestParam String titulo,
            @RequestParam(required = false) String autor,
            Authentication authentication) {

        Map<String, Object> response = new HashMap<>();

        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Long usuarioId = userDetails.getId();

            logger.info("Intentando eliminar libro de favoritos: {} por usuario: {}", titulo, usuarioId);

            libroFavoritoService.eliminarDeFavoritos(usuarioId, titulo, autor);

            response.put("success", true);
            response.put("message", "Libro eliminado de favoritos");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            logger.error("Error al eliminar libro de favoritos: {}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            logger.error("Error inesperado al eliminar libro de favoritos", e);
            response.put("success", false);
            response.put("message", "Error interno del servidor");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Eliminar libro favorito por ID (AJAX)
     */
    @DeleteMapping("/eliminar/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> eliminarLibroFavorito(
            @PathVariable Long id,
            Authentication authentication) {

        Map<String, Object> response = new HashMap<>();

        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Long usuarioId = userDetails.getId();

            libroFavoritoService.eliminarLibroFavorito(id, usuarioId);

            response.put("success", true);
            response.put("message", "Libro eliminado de favoritos");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            logger.error("Error al eliminar libro favorito ID {}: {}", id, e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            logger.error("Error inesperado al eliminar libro favorito ID: {}", id, e);
            response.put("success", false);
            response.put("message", "Error interno del servidor");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Verificar si un libro está en favoritos (AJAX)
     */
    @GetMapping("/verificar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> verificarFavorito(
            @RequestParam String titulo,
            @RequestParam(required = false) String autor,
            Authentication authentication) {

        Map<String, Object> response = new HashMap<>();

        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Long usuarioId = userDetails.getId();

            boolean esFavorito = libroFavoritoService.esLibroFavorito(usuarioId, titulo, autor);

            response.put("success", true);
            response.put("esFavorito", esFavorito);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error al verificar libro favorito", e);
            response.put("success", false);
            response.put("message", "Error al verificar favorito");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Mostrar página de libros favoritos
     */
    @GetMapping
    public String mostrarLibrosFavoritos(Model model, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long usuarioId = userDetails.getId();

        List<LibroFavorito> librosFavoritos = libroFavoritoService.obtenerLibrosFavoritos(usuarioId);
        long totalFavoritos = libroFavoritoService.contarLibrosFavoritos(usuarioId);

        model.addAttribute("librosFavoritos", librosFavoritos);
        model.addAttribute("totalFavoritos", totalFavoritos);
        model.addAttribute("usuario", userDetails.getUsuario());

        return "libros-favoritos";
    }

    /**
     * Obtener libros favoritos como JSON (AJAX)
     */
    @GetMapping("/json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerLibrosFavoritosJson(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Long usuarioId = userDetails.getId();

            List<LibroFavorito> librosFavoritos = libroFavoritoService.obtenerLibrosFavoritos(usuarioId);

            response.put("success", true);
            response.put("libros", librosFavoritos);
            response.put("total", librosFavoritos.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error al obtener libros favoritos", e);
            response.put("success", false);
            response.put("message", "Error al obtener libros favoritos");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}