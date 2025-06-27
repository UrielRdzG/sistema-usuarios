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

