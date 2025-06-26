-- Crear base de datos
CREATE DATABASE IF NOT EXISTS sistema_usuarios;
USE sistema_usuarios;

-- Crear tabla usuarios
CREATE TABLE IF NOT EXISTS usuarios (
                                        id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                        nombre VARCHAR(50) NOT NULL,
    apellido_paterno VARCHAR(50) NOT NULL,
    apellido_materno VARCHAR(50) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    numero_telefonico VARCHAR(10),
    username VARCHAR(20) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    rol ENUM('USUARIO', 'ADMINISTRADOR') NOT NULL DEFAULT 'USUARIO',
    foto_perfil LONGBLOB,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    activo BOOLEAN NOT NULL DEFAULT TRUE
    );

-- Índices para optimización
CREATE INDEX idx_usuarios_username ON usuarios(username);
CREATE INDEX idx_usuarios_email ON usuarios(email);
CREATE INDEX idx_usuarios_rol ON usuarios(rol);
CREATE INDEX idx_usuarios_activo ON usuarios(activo);

-- Insertar usuario administrador por defecto (la contraseña será encriptada por la aplicación)
-- Usuario: admin, Contraseña: admin123
INSERT INTO usuarios (nombre, apellido_paterno, apellido_materno, email, numero_telefonico, username, password, rol, activo)
VALUES ('Administrador', 'Sistema', 'Principal', 'admin@sistema.com', '1234567890', 'admin', 'admin123', 'ADMINISTRADOR', TRUE)
    ON DUPLICATE KEY UPDATE id=id;

-- Insertar usuario de prueba (la contraseña será encriptada por la aplicación)
-- Usuario: usuario1, Contraseña: 123456
INSERT INTO usuarios (nombre, apellido_paterno, apellido_materno, email, numero_telefonico, username, password, rol, activo)
VALUES ('Juan', 'Pérez', 'García', 'juan@ejemplo.com', '9876543210', 'usuario1', '123456', 'USUARIO', TRUE)
    ON DUPLICATE KEY UPDATE id=id;

CREATE TABLE IF NOT EXISTS libro_recomendaciones (
                                                     id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                     titulo VARCHAR(255) NOT NULL,
    autor VARCHAR(500),
    año_publicacion INT,
    cover_id VARCHAR(100),
    open_library_key VARCHAR(100),
    temas VARCHAR(1000),
    fecha_guardado DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    notas_personales VARCHAR(1000),
    estado_lectura ENUM('QUIERO_LEER', 'LEYENDO', 'LEIDO', 'PAUSADO', 'ABANDONADO') DEFAULT 'QUIERO_LEER',
    usuario_id BIGINT NOT NULL,

    -- Índices para optimizar consultas
    INDEX idx_usuario_fecha (usuario_id, fecha_guardado),
    INDEX idx_usuario_estado (usuario_id, estado_lectura),
    INDEX idx_open_library_key (open_library_key),

    -- Clave foránea
    CONSTRAINT fk_libro_usuario
    FOREIGN KEY (usuario_id)
    REFERENCES usuarios(id)
    ON DELETE CASCADE
    );

-- Crear índice único para evitar duplicados por usuario
CREATE UNIQUE INDEX idx_usuario_libro_unique
    ON libro_recomendaciones(usuario_id, open_library_key);

-- Comentarios para documentar la tabla
ALTER TABLE libro_recomendaciones
    COMMENT = 'Tabla para almacenar recomendaciones personales de libros por usuario';

-- Verificar que la tabla se creó correctamente
DESCRIBE libro_recomendaciones;