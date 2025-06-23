package com.example.sistema_usuarios.controller;

import com.example.sistema_usuarios.model.Usuario; // Importa la clase Usuario
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Maneja cualquier excepción inesperada que ocurra en los controladores.
     */
    @ExceptionHandler(Exception.class)
    public String handleGlobalException(Exception e, Model model) {
        // Imprimimos el error original y completo en la consola
        logger.error("¡ERROR ORIGINAL CAPTURADO!", e);

        // Añadimos un mensaje amigable para el usuario
        model.addAttribute("error", "Ocurrió un error inesperado. Revisa los datos e intenta de nuevo.");
        
        // **** LA SOLUCIÓN ESTÁ AQUÍ ****
        // Añadimos un objeto Usuario vacío al modelo para que el formulario de registro no falle.
        if (!model.containsAttribute("usuario")) {
            model.addAttribute("usuario", new Usuario());
        }
        
        // Devolvemos al usuario a la página de registro
        return "registro";
    }

    /**
     * Maneja específicamente los errores cuando un archivo es demasiado grande.
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxSizeException(MaxUploadSizeExceededException e, Model model) {
        logger.warn("Se intentó subir un archivo más grande que el límite permitido.", e);
        model.addAttribute("error", "El archivo que intentas subir es demasiado grande. El límite es de 10MB.");
        
        // Añadimos también aquí el objeto Usuario para evitar el error de renderizado
        if (!model.containsAttribute("usuario")) {
            model.addAttribute("usuario", new Usuario());
        }
        
        return "registro";
    }
}