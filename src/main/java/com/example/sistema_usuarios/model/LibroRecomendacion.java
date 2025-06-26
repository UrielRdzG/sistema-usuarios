package com.example.sistema_usuarios.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "libro_recomendaciones")
public class LibroRecomendacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(length = 500)
    private String autor;

    @Column(name = "año_publicacion")
    private Integer añoPublicacion;

    @Column(name = "cover_id")
    private String coverId; // ID de la portada de Open Library

    @Column(name = "open_library_key")
    private String openLibraryKey; // Clave única del libro en Open Library

    @Column(length = 1000)
    private String temas; // Temas/géneros separados por comas

    @Column(name = "fecha_guardado")
    private LocalDateTime fechaGuardado;

    @Column(length = 1000)
    private String notasPersonales; // Notas del usuario sobre el libro

    @Enumerated(EnumType.STRING)
    private EstadoLectura estadoLectura;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    // Constructores
    public LibroRecomendacion() {
        this.fechaGuardado = LocalDateTime.now();
        this.estadoLectura = EstadoLectura.QUIERO_LEER;
    }

    public LibroRecomendacion(String titulo, String autor, Integer añoPublicacion, Usuario usuario) {
        this();
        this.titulo = titulo;
        this.autor = autor;
        this.añoPublicacion = añoPublicacion;
        this.usuario = usuario;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Integer getAñoPublicacion() {
        return añoPublicacion;
    }

    public void setAñoPublicacion(Integer añoPublicacion) {
        this.añoPublicacion = añoPublicacion;
    }

    public String getCoverId() {
        return coverId;
    }

    public void setCoverId(String coverId) {
        this.coverId = coverId;
    }

    public String getOpenLibraryKey() {
        return openLibraryKey;
    }

    public void setOpenLibraryKey(String openLibraryKey) {
        this.openLibraryKey = openLibraryKey;
    }

    public String getTemas() {
        return temas;
    }

    public void setTemas(String temas) {
        this.temas = temas;
    }

    public LocalDateTime getFechaGuardado() {
        return fechaGuardado;
    }

    public void setFechaGuardado(LocalDateTime fechaGuardado) {
        this.fechaGuardado = fechaGuardado;
    }

    public String getNotasPersonales() {
        return notasPersonales;
    }

    public void setNotasPersonales(String notasPersonales) {
        this.notasPersonales = notasPersonales;
    }

    public EstadoLectura getEstadoLectura() {
        return estadoLectura;
    }

    public void setEstadoLectura(EstadoLectura estadoLectura) {
        this.estadoLectura = estadoLectura;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    // Método para obtener la URL de la portada
    public String getCoverUrl() {
        if (coverId != null && !coverId.isEmpty()) {
            return "https://covers.openlibrary.org/b/id/" + coverId + "-M.jpg";
        }
        return null;
    }

    // Enum para el estado de lectura
    public enum EstadoLectura {
        QUIERO_LEER("Quiero leer"),
        LEYENDO("Leyendo"),
        LEIDO("Leído"),
        PAUSADO("Pausado"),
        ABANDONADO("Abandonado");

        private final String descripcion;

        EstadoLectura(String descripcion) {
            this.descripcion = descripcion;
        }

        public String getDescripcion() {
            return descripcion;
        }
    }
}