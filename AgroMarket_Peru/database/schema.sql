CREATE DATABASE IF NOT EXISTS agromarketperu CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE agromarketperu;
CREATE TABLE IF NOT EXISTS usuarios (
  id INT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(120) NOT NULL,
  correo VARCHAR(160) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  rol ENUM('user','admin') NOT NULL DEFAULT 'user',
  telefono VARCHAR(30),
  dni VARCHAR(20),
  direccion VARCHAR(255),
  distrito VARCHAR(100),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS sesiones (
  token CHAR(64) PRIMARY KEY,
  usuario_id INT NOT NULL,
  expires_at DATETIME NOT NULL,
  FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS pedidos (
  id INT AUTO_INCREMENT PRIMARY KEY,
  numero VARCHAR(30) NOT NULL UNIQUE,
  usuario_id INT NOT NULL,
  nombre_cliente VARCHAR(120) NOT NULL,
  correo VARCHAR(160) NOT NULL,
  telefono VARCHAR(30) NOT NULL,
  dni VARCHAR(20) NOT NULL,
  direccion VARCHAR(255) NOT NULL,
  distrito VARCHAR(100) NOT NULL,
  referencia VARCHAR(255),
  metodo_pago VARCHAR(60) NOT NULL,
  total DECIMAL(10,2) NOT NULL,
  estado VARCHAR(40) NOT NULL DEFAULT 'Confirmado',
  fecha_entrega VARCHAR(80) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS detalle_pedido (
  id INT AUTO_INCREMENT PRIMARY KEY,
  pedido_id INT NOT NULL,
  producto_id INT NOT NULL,
  producto_nombre VARCHAR(180) NOT NULL,
  tienda VARCHAR(100) NOT NULL,
  precio DECIMAL(10,2) NOT NULL,
  cantidad INT NOT NULL,
  subtotal DECIMAL(10,2) NOT NULL,
  FOREIGN KEY (pedido_id) REFERENCES pedidos(id) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS alertas (
  id INT AUTO_INCREMENT PRIMARY KEY,
  usuario_id INT NOT NULL,
  producto_id INT NOT NULL,
  producto_nombre VARCHAR(180) NOT NULL,
  precio_objetivo DECIMAL(10,2) NOT NULL,
  activo TINYINT(1) NOT NULL DEFAULT 1,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);
