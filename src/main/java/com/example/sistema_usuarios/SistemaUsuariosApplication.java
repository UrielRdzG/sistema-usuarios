package com.example.sistema_usuarios;

import com.example.sistema_usuarios.model.Usuario;
import com.example.sistema_usuarios.model.Rol;
import com.example.sistema_usuarios.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SistemaUsuariosApplication implements CommandLineRunner {

	@Autowired
	private UsuarioService usuarioService;

	public static void main(String[] args) {
		SpringApplication.run(SistemaUsuariosApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		// Crear usuario administrador por defecto si no existe
		if (!usuarioService.existeUsername("admin")) {
			Usuario admin = new Usuario();
			admin.setNombre("Administrador");
			admin.setApellidoPaterno("Sistema");
			admin.setApellidoMaterno("Principal");
			admin.setEmail("admin@sistema.com");
			admin.setNumeroTelefonico("1234567890");
			admin.setUsername("admin");
			admin.setPassword("admin123");
			admin.setRol(Rol.ADMINISTRADOR);

			usuarioService.crearUsuario(admin);
			System.out.println("Usuario administrador creado: admin/admin123");
		}

		// Crear usuario de prueba si no existe
		if (!usuarioService.existeUsername("usuario1")) {
			Usuario usuario = new Usuario();
			usuario.setNombre("Juan");
			usuario.setApellidoPaterno("Pérez");
			usuario.setApellidoMaterno("García");
			usuario.setEmail("juan@ejemplo.com");
			usuario.setNumeroTelefonico("9876543210");
			usuario.setUsername("usuario1");
			usuario.setPassword("123456");
			usuario.setRol(Rol.USUARIO);

			usuarioService.crearUsuario(usuario);
			System.out.println("Usuario de prueba creado: usuario1/123456");
		}
	}
}
