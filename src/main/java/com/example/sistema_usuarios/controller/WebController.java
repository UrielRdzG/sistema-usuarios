package com.example.sistema_usuarios.controller;

import com.example.sistema_usuarios.model.Usuario;
import com.example.sistema_usuarios.model.Rol;
import com.example.sistema_usuarios.service.UsuarioService;
import com.example.sistema_usuarios.security.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.Optional;

@Controller
public class WebController {

    @Autowired
    private UsuarioService usuarioService;

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

    // Procesar registro
    @PostMapping("/registro")
    public String procesarRegistro(@Valid @ModelAttribute("usuario") Usuario usuario,
                                   BindingResult result,
                                   @RequestParam("confirmarPassword") String confirmarPassword,
                                   @RequestParam(value = "fotoPerfil", required = false) MultipartFile fotoPerfil,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {

        // Validaciones adicionales
        if (result.hasErrors()) {
            return "registro";
        }

        if (!usuario.getPassword().equals(confirmarPassword)) {
            result.rejectValue("password", "error.usuario", "Las contraseñas no coinciden");
            return "registro";
        }

        if (usuarioService.existeUsername(usuario.getUsername())) {
            result.rejectValue("username", "error.usuario", "El nombre de usuario ya existe");
            return "registro";
        }

        if (usuarioService.existeEmail(usuario.getEmail())) {
            result.rejectValue("email", "error.usuario", "El correo electrónico ya está registrado");
            return "registro";
        }

        try {
            // Procesar foto de perfil
            if (fotoPerfil != null && !fotoPerfil.isEmpty()) {
                usuario.setFotoPerfil(fotoPerfil.getBytes());
            }

            // Registrar usuario
            usuarioService.registrarUsuario(usuario);
            redirectAttributes.addFlashAttribute("mensaje", "Usuario registrado exitosamente");
            return "redirect:/login";

        } catch (IOException e) {
            model.addAttribute("error", "Error al procesar la imagen");
            return "registro";
        } catch (Exception e) {
            model.addAttribute("error", "Error al registrar usuario");
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
            // Procesar foto de perfil
            if (fotoPerfil != null && !fotoPerfil.isEmpty()) {
                usuario.setFotoPerfil(fotoPerfil.getBytes());
            }

            // Actualizar usuario
            usuario.setId(usuarioId);
            usuarioService.actualizarPerfil(usuario, nuevaPassword);

            redirectAttributes.addFlashAttribute("mensaje", "Perfil actualizado exitosamente");
            return "redirect:/perfil";

        } catch (Exception e) {
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