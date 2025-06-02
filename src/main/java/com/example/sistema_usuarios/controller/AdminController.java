package com.example.sistema_usuarios.controller;

import com.example.sistema_usuarios.model.Usuario;
import com.example.sistema_usuarios.model.Rol;
import com.example.sistema_usuarios.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class AdminController {

    @Autowired
    private UsuarioService usuarioService;

    // Panel de administración
    @GetMapping
    public String panelAdmin(Model model) {
        model.addAttribute("totalUsuarios", usuarioService.contarUsuariosActivos());
        model.addAttribute("totalAdmins", usuarioService.contarUsuariosPorRol(Rol.ADMINISTRADOR));
        model.addAttribute("totalUsuariosComunes", usuarioService.contarUsuariosPorRol(Rol.USUARIO));
        return "admin/panel";
    }

    // Listar todos los usuarios
    @GetMapping("/usuarios")
    public String listarUsuarios(@RequestParam(value = "busqueda", required = false) String busqueda,
                                 @RequestParam(value = "rol", required = false) String rol,
                                 Model model) {
        List<Usuario> usuarios;

        if (busqueda != null && !busqueda.trim().isEmpty()) {
            usuarios = usuarioService.buscarUsuariosPorNombre(busqueda);
        } else if (rol != null && !rol.isEmpty()) {
            try {
                Rol rolEnum = Rol.valueOf(rol.toUpperCase());
                usuarios = usuarioService.listarUsuariosPorRol(rolEnum);
            } catch (IllegalArgumentException e) {
                usuarios = usuarioService.listarUsuariosActivos();
            }
        } else {
            usuarios = usuarioService.listarUsuariosActivos();
        }

        model.addAttribute("usuarios", usuarios);
        model.addAttribute("busqueda", busqueda);
        model.addAttribute("rolSeleccionado", rol);
        model.addAttribute("roles", Rol.values());

        return "admin/usuarios";
    }

    // Mostrar formulario para crear nuevo usuario
    @GetMapping("/usuarios/nuevo")
    public String mostrarFormularioNuevoUsuario(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("roles", Rol.values());
        return "admin/nuevo-usuario";
    }

    // Procesar creación de nuevo usuario
    @PostMapping("/usuarios/nuevo")
    public String procesarNuevoUsuario(@Valid @ModelAttribute("usuario") Usuario usuario,
                                       BindingResult result,
                                       @RequestParam("confirmarPassword") String confirmarPassword,
                                       @RequestParam(value = "fotoPerfil", required = false) MultipartFile fotoPerfil,
                                       Model model,
                                       RedirectAttributes redirectAttributes) {

        // Validaciones
        if (result.hasErrors()) {
            model.addAttribute("roles", Rol.values());
            return "admin/nuevo-usuario";
        }

        if (!usuario.getPassword().equals(confirmarPassword)) {
            result.rejectValue("password", "error.usuario", "Las contraseñas no coinciden");
            model.addAttribute("roles", Rol.values());
            return "admin/nuevo-usuario";
        }

        if (usuarioService.existeUsername(usuario.getUsername())) {
            result.rejectValue("username", "error.usuario", "El nombre de usuario ya existe");
            model.addAttribute("roles", Rol.values());
            return "admin/nuevo-usuario";
        }

        if (usuarioService.existeEmail(usuario.getEmail())) {
            result.rejectValue("email", "error.usuario", "El correo electrónico ya está registrado");
            model.addAttribute("roles", Rol.values());
            return "admin/nuevo-usuario";
        }

        try {
            // Procesar foto de perfil
            if (fotoPerfil != null && !fotoPerfil.isEmpty()) {
                usuario.setFotoPerfil(fotoPerfil.getBytes());
            }

            // Crear usuario
            usuarioService.crearUsuario(usuario);
            redirectAttributes.addFlashAttribute("mensaje", "Usuario creado exitosamente");
            return "redirect:/admin/usuarios";

        } catch (IOException e) {
            model.addAttribute("error", "Error al procesar la imagen");
            model.addAttribute("roles", Rol.values());
            return "admin/nuevo-usuario";
        } catch (Exception e) {
            model.addAttribute("error", "Error al crear usuario");
            model.addAttribute("roles", Rol.values());
            return "admin/nuevo-usuario";
        }
    }

    // Ver detalles de un usuario
    @GetMapping("/usuarios/{id}")
    public String verUsuario(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Usuario> usuario = usuarioService.obtenerUsuarioPorId(id);
        if (usuario.isPresent()) {
            model.addAttribute("usuario", usuario.get());
            return "admin/ver-usuario";
        } else {
            redirectAttributes.addFlashAttribute("error", "Usuario no encontrado");
            return "redirect:/admin/usuarios";
        }
    }

    // Mostrar formulario para editar usuario
    @GetMapping("/usuarios/{id}/editar")
    public String mostrarFormularioEditarUsuario(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Usuario> usuario = usuarioService.obtenerUsuarioPorId(id);
        if (usuario.isPresent()) {
            model.addAttribute("usuario", usuario.get());
            model.addAttribute("roles", Rol.values());
            return "admin/editar-usuario";
        } else {
            redirectAttributes.addFlashAttribute("error", "Usuario no encontrado");
            return "redirect:/admin/usuarios";
        }
    }

    // Procesar edición de usuario
    @PostMapping("/usuarios/{id}/editar")
    public String procesarEditarUsuario(@PathVariable Long id,
                                        @Valid @ModelAttribute("usuario") Usuario usuario,
                                        BindingResult result,
                                        @RequestParam(value = "nuevaPassword", required = false) String nuevaPassword,
                                        @RequestParam(value = "confirmarPassword", required = false) String confirmarPassword,
                                        @RequestParam(value = "fotoPerfil", required = false) MultipartFile fotoPerfil,
                                        Model model,
                                        RedirectAttributes redirectAttributes) {

        // Validaciones
        if (result.hasErrors()) {
            model.addAttribute("roles", Rol.values());
            return "admin/editar-usuario";
        }

        // Verificar username único (excluyendo el usuario actual)
        if (usuarioService.existeUsernameExcluyendoUsuario(usuario.getUsername(), id)) {
            result.rejectValue("username", "error.usuario", "El nombre de usuario ya existe");
            model.addAttribute("roles", Rol.values());
            return "admin/editar-usuario";
        }

        // Verificar email único (excluyendo el usuario actual)
        if (usuarioService.existeEmailExcluyendoUsuario(usuario.getEmail(), id)) {
            result.rejectValue("email", "error.usuario", "El correo electrónico ya está registrado");
            model.addAttribute("roles", Rol.values());
            return "admin/editar-usuario";
        }

        // Validar nueva contraseña si se proporciona
        if (nuevaPassword != null && !nuevaPassword.trim().isEmpty()) {
            if (nuevaPassword.length() < 6) {
                model.addAttribute("errorPassword", "La contraseña debe tener al menos 6 caracteres");
                model.addAttribute("roles", Rol.values());
                return "admin/editar-usuario";
            }
            if (!nuevaPassword.equals(confirmarPassword)) {
                model.addAttribute("errorPassword", "Las contraseñas no coinciden");
                model.addAttribute("roles", Rol.values());
                return "admin/editar-usuario";
            }
        }

        try {
            // Procesar foto de perfil
            if (fotoPerfil != null && !fotoPerfil.isEmpty()) {
                usuario.setFotoPerfil(fotoPerfil.getBytes());
            }

            // Actualizar usuario
            usuario.setId(id);
            usuarioService.actualizarPerfil(usuario, nuevaPassword);

            redirectAttributes.addFlashAttribute("mensaje", "Usuario actualizado exitosamente");
            return "redirect:/admin/usuarios/" + id;

        } catch (Exception e) {
            model.addAttribute("error", "Error al actualizar el usuario");
            model.addAttribute("roles", Rol.values());
            return "admin/editar-usuario";
        }
    }

    // Cambiar rol de usuario
    @PostMapping("/usuarios/{id}/cambiar-rol")
    public String cambiarRol(@PathVariable Long id,
                             @RequestParam("rol") String rol,
                             RedirectAttributes redirectAttributes) {
        try {
            Rol nuevoRol = Rol.valueOf(rol.toUpperCase());
            usuarioService.cambiarRol(id, nuevoRol);
            redirectAttributes.addFlashAttribute("mensaje", "Rol actualizado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cambiar el rol");
        }
        return "redirect:/admin/usuarios/" + id;
    }

    // Cambiar estado de usuario (activar/desactivar)
    @PostMapping("/usuarios/{id}/cambiar-estado")
    public String cambiarEstado(@PathVariable Long id,
                                @RequestParam("activo") boolean activo,
                                RedirectAttributes redirectAttributes) {
        try {
            usuarioService.cambiarEstadoUsuario(id, activo);
            String mensaje = activo ? "Usuario activado exitosamente" : "Usuario desactivado exitosamente";
            redirectAttributes.addFlashAttribute("mensaje", mensaje);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cambiar el estado del usuario");
        }
        return "redirect:/admin/usuarios/" + id;
    }

    // Eliminar usuario (soft delete)
    @PostMapping("/usuarios/{id}/eliminar")
    public String eliminarUsuario(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.eliminarUsuario(id);
            redirectAttributes.addFlashAttribute("mensaje", "Usuario eliminado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar el usuario");
        }
        return "redirect:/admin/usuarios";
    }

    // Eliminar usuario permanentemente
    @PostMapping("/usuarios/{id}/eliminar-permanente")
    public String eliminarUsuarioPermanente(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.eliminarUsuarioPermanentemente(id);
            redirectAttributes.addFlashAttribute("mensaje", "Usuario eliminado permanentemente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar el usuario permanentemente");
        }
        return "redirect:/admin/usuarios";
    }
}
