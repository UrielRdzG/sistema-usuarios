package com.example.sistema_usuarios.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "libros_favoritos",
        uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "titulo", "autor"}))
public class LibroFavorito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @NotNull(message = "El usuario es obligatorio")
    private Usuario usuario;

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 500, message = "El título no puede exceder 500 caracteres")
    @Column(nullable = false, length = 500)
    private String titulo;

    @Size(max = 300, message = "El autor no puede exceder 300 caracteres")
    @Column(length = 300)
    private String autor;

    @Size(max = 20, message = "El año de publicación no puede exceder 20 caracteres")
    @Column(name = "año_publicacion", length = 20)
    private String añoPublicacion;

    @Size(max = 50, message = "El cover_id no puede exceder 50 caracteres")
    @Column(name = "cover_id", length = 50)
    private String coverId;

    @Column(columnDefinition = "TEXT")
    private String temas;

    @Column(name = "fecha_agregado", nullable = false)
    private LocalDateTime fechaAgregado;

    // Constructores
    public LibroFavorito() {
        this.fechaAgregado = LocalDateTime.now();
    }

    public LibroFavorito(Usuario usuario, String titulo, String autor, String añoPublicacion,
                         String coverId, String temas) {
        this();
        this.usuario = usuario;
        this.titulo = titulo;
        this.autor = autor;
        this.añoPublicacion = añoPublicacion;
        this.coverId = coverId;
        this.temas = temas;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getAñoPublicacion() {
        return añoPublicacion;
    }

    public void setAñoPublicacion(String añoPublicacion) {
        this.añoPublicacion = añoPublicacion;
    }

    public String getCoverId() {
        return coverId;
    }

    public void setCoverId(String coverId) {
        this.coverId = coverId;
    }

    public String getTemas() {
        return temas;
    }

    public void setTemas(String temas) {
        this.temas = temas;
    }

    public LocalDateTime getFechaAgregado() {
        return fechaAgregado;
    }

    public void setFechaAgregado(LocalDateTime fechaAgregado) {
        this.fechaAgregado = fechaAgregado;
    }

    // Método para obtener la URL de la portada
    public String getCoverUrl() {
        if (coverId != null && !coverId.isEmpty()) {
            return "https://covers.openlibrary.org/b/id/" + coverId + "-M.jpg";
        }
        return null;
    }

    @Override
    public String toString() {
        return "LibroFavorito{" +
                "id=" + id +
                ", titulo='" + titulo + '\'' +
                ", autor='" + autor + '\'' +
                ", añoPublicacion='" + añoPublicacion + '\'' +
                ", fechaAgregado=" + fechaAgregado +
                '}';
    }
}