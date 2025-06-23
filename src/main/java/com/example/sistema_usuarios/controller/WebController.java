package com.example.sistema_usuarios.controller;

import com.example.sistema_usuarios.model.Rol;
import com.example.sistema_usuarios.model.Usuario;
import com.example.sistema_usuarios.security.CustomUserDetails;
import com.example.sistema_usuarios.service.UsuarioService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.Optional;

@Controller
public class WebController {

    private static final Logger logger = LoggerFactory.getLogger(WebController.class);

    @Autowired
    private UsuarioService usuarioService;

    /**
     * Este método se utiliza para configurar el data binder.
     * Aquí le decimos a Spring que no intente vincular el campo 'fotoPerfil' automáticamente.
     * Esto previene el error de conversión de MultipartFile a byte[], permitiéndonos
     * manejar la conversión del archivo manualmente en nuestro método de controlador.
     * @param binder El WebDataBinder para la solicitud actual.
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("fotoPerfil");
    }

    // Página principal
    @GetMapping("/")
    public String index() {
        return "index";
    }

    // Página de login
    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        Model model) {
        if (error != null) {
            model.addAttribute("error", "Usuario o contraseña incorrectos");
        }
        if (logout != null) {
            model.addAttribute("mensaje", "Has cerrado sesión correctamente");
        }
        return "login";
    }

    // Página de registro
    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "registro";
    }

    // Procesar registro (versión robusta con logging)
    @PostMapping("/registro")
    public String procesarRegistro(@Valid @ModelAttribute("usuario") Usuario usuario,
                                   BindingResult result,
                                   @RequestParam("confirmarPassword") String confirmarPassword,
                                   @RequestParam(value = "fotoPerfil", required = false) MultipartFile fotoPerfil,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {

        logger.info("--- INICIANDO PROCESO DE REGISTRO para usuario: {} ---", usuario.getUsername());

        // 1. Validación de contraseña (se añade a los errores de @Valid)
        if (usuario.getPassword() != null && !usuario.getPassword().equals(confirmarPassword)) {
            logger.warn("Validación fallida: Las contraseñas no coinciden.");
            result.rejectValue("password", "error.usuario", "Las contraseñas no coinciden");
        }

        // 2. Validaciones de negocio (existencia de username/email)
        if (usuario.getUsername() != null && !usuario.getUsername().isBlank() && usuarioService.existeUsername(usuario.getUsername())) {
            logger.warn("Validación fallida: El username '{}' ya existe.", usuario.getUsername());
            result.rejectValue("username", "error.usuario", "El nombre de usuario ya existe");
        }

        if (usuario.getEmail() != null && !usuario.getEmail().isBlank() && usuarioService.existeEmail(usuario.getEmail())) {
            logger.warn("Validación fallida: El email '{}' ya existe.", usuario.getEmail());
            result.rejectValue("email", "error.usuario", "El correo electrónico ya está registrado");
        }

        // 3. Comprobar si hubo algún error de validación en total
        if (result.hasErrors()) {
            logger.error("El formulario contiene errores de validación. Volviendo a la página de registro. Errores: {}", result.getAllErrors());
            model.addAttribute("error", "Por favor, corrige los errores resaltados en el formulario.");
            return "registro";
        }

        // 4. Si todas las validaciones pasan, se procede a guardar
        try {
            logger.info("Todas las validaciones pasaron. Intentando guardar el usuario.");

            if (fotoPerfil != null && !fotoPerfil.isEmpty()) {
                usuario.setFotoPerfil(fotoPerfil.getBytes());
                logger.info("Foto de perfil procesada y lista para guardar.");
            }

            usuarioService.registrarUsuario(usuario);
            logger.info("Usuario '{}' guardado en la base de datos exitosamente.", usuario.getUsername());

            redirectAttributes.addFlashAttribute("mensaje", "¡Usuario registrado exitosamente! Ya puedes iniciar sesión.");

            logger.info("--- REGISTRO COMPLETADO. Redirigiendo a /login. ---");
            return "redirect:/login";

        } catch (IOException e) {
            logger.error("Ocurrió una excepción de E/S (IOException) durante el guardado del usuario, probablemente con la imagen.", e);
            model.addAttribute("error", "Ocurrió un error al procesar la imagen de perfil.");
            return "registro";
        } catch (Exception e) {
            logger.error("Ocurrió una excepción inesperada durante el guardado del usuario.", e);
            model.addAttribute("error", "Ocurrió un error interno al crear la cuenta. Por favor, inténtelo de nuevo más tarde.");
            return "registro";
        }
    }

    // Dashboard principal después del login
    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        model.addAttribute("usuario", userDetails.getUsuario());

        if (userDetails.isAdmin()) {
            // Estadísticas para administradores
            model.addAttribute("totalUsuarios", usuarioService.contarUsuariosActivos());
            model.addAttribute("totalAdmins", usuarioService.contarUsuariosPorRol(Rol.ADMINISTRADOR));
            model.addAttribute("totalUsuariosComunes", usuarioService.contarUsuariosPorRol(Rol.USUARIO));
        }

        return "dashboard";
    }

    // Perfil del usuario
    @GetMapping("/perfil")
    public String mostrarPerfil(Model model, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        model.addAttribute("usuario", userDetails.getUsuario());
        return "perfil";
    }

    // Editar perfil
    @GetMapping("/perfil/editar")
    public String mostrarEditarPerfil(Model model, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        model.addAttribute("usuario", userDetails.getUsuario());
        return "editar-perfil";
    }

    // Procesar edición de perfil
    @PostMapping("/perfil/editar")
    public String procesarEditarPerfil(@Valid @ModelAttribute("usuario") Usuario usuario,
                                       BindingResult result,
                                       @RequestParam(value = "nuevaPassword", required = false) String nuevaPassword,
                                       @RequestParam(value = "confirmarPassword", required = false) String confirmarPassword,
                                       @RequestParam(value = "fotoPerfil", required = false) MultipartFile fotoPerfil,
                                       Authentication authentication,
                                       Model model,
                                       RedirectAttributes redirectAttributes) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long usuarioId = userDetails.getId();

        // Validaciones adicionales
        if (result.hasErrors()) {
            return "editar-perfil";
        }

        // Verificar username único (excluyendo el usuario actual)
        if (usuarioService.existeUsernameExcluyendoUsuario(usuario.getUsername(), usuarioId)) {
            result.rejectValue("username", "error.usuario", "El nombre de usuario ya existe");
            return "editar-perfil";
        }

        // Verificar email único (excluyendo el usuario actual)
        if (usuarioService.existeEmailExcluyendoUsuario(usuario.getEmail(), usuarioId)) {
            result.rejectValue("email", "error.usuario", "El correo electrónico ya está registrado");
            return "editar-perfil";
        }

        // Validar nueva contraseña si se proporciona
        if (nuevaPassword != null && !nuevaPassword.trim().isEmpty()) {
            if (nuevaPassword.length() < 6) {
                model.addAttribute("errorPassword", "La contraseña debe tener al menos 6 caracteres");
                return "editar-perfil";
            }
            if (!nuevaPassword.equals(confirmarPassword)) {
                model.addAttribute("errorPassword", "Las contraseñas no coinciden");
                return "editar-perfil";
            }
        }

        try {
            // Actualizar usuario
            Usuario usuarioExistente = usuarioService.obtenerUsuarioPorId(usuarioId)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado para actualizar. ID: " + usuarioId));

            // Actualizar solo los campos necesarios del objeto existente
            usuarioExistente.setNombre(usuario.getNombre());
            usuarioExistente.setApellidoPaterno(usuario.getApellidoPaterno());
            usuarioExistente.setApellidoMaterno(usuario.getApellidoMaterno());
            usuarioExistente.setEmail(usuario.getEmail());
            usuarioExistente.setNumeroTelefonico(usuario.getNumeroTelefonico());
            usuarioExistente.setUsername(usuario.getUsername());

            // Procesar foto de perfil si se subió una nueva
            if (fotoPerfil != null && !fotoPerfil.isEmpty()) {
                usuarioExistente.setFotoPerfil(fotoPerfil.getBytes());
            }

            usuarioService.actualizarPerfil(usuarioExistente, nuevaPassword);

            redirectAttributes.addFlashAttribute("mensaje", "Perfil actualizado exitosamente");
            return "redirect:/perfil";

        } catch (Exception e) {
            logger.error("Error al actualizar el perfil para el usuario ID: {}", usuarioId, e);
            model.addAttribute("error", "Error al actualizar el perfil");
            return "editar-perfil";
        }
    }

    // Eliminar cuenta propia
    @PostMapping("/perfil/eliminar")
    public String eliminarCuentaPropia(Authentication authentication,
                                       RedirectAttributes redirectAttributes) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        try {
            usuarioService.eliminarUsuario(userDetails.getId());
            redirectAttributes.addFlashAttribute("mensaje", "Cuenta eliminada exitosamente");
            return "redirect:/logout";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar la cuenta");
            return "redirect:/perfil";
        }
    }

    // Mostrar foto de perfil
    @GetMapping("/usuario/foto/{id}")
    @ResponseBody
    public byte[] mostrarFotoPerfil(@PathVariable Long id) {
        Optional<Usuario> usuario = usuarioService.obtenerUsuarioPorId(id);
        if (usuario.isPresent() && usuario.get().getFotoPerfil() != null) {
            return usuario.get().getFotoPerfil();
        }
        return new byte[0];
    }
}
