-- =====================================================================
--  Hamburguesería - Script de base de datos (MySQL 8.0.16+)
--  Contiene: creación de BD, tablas, PK/FK, stored procedures,
--  triggers y datos de prueba (seeders).
--
--  Ejecutar completo, por ejemplo:
--     mysql -u root -p < script_bd.sql
--  o desde MySQL Workbench (File > Open SQL Script > Ejecutar todo).
-- =====================================================================

DROP DATABASE IF EXISTS hamburgueseria;
CREATE DATABASE hamburgueseria
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE hamburgueseria;

-- ---------------------------------------------------------------------
--  TABLAS
-- ---------------------------------------------------------------------

-- Categoría (1) ---- (N) Producto
CREATE TABLE categoria (
    id_categoria INT          NOT NULL AUTO_INCREMENT,
    nombre       VARCHAR(60)  NOT NULL,
    activo       TINYINT(1)   NOT NULL DEFAULT 1,
    CONSTRAINT pk_categoria        PRIMARY KEY (id_categoria),
    CONSTRAINT uq_categoria_nombre UNIQUE (nombre)
) ENGINE = InnoDB;

CREATE TABLE producto (
    id_producto  INT           NOT NULL AUTO_INCREMENT,
    codigo       VARCHAR(10)   NOT NULL,
    nombre       VARCHAR(100)  NOT NULL,
    precio       DECIMAL(10,2) NOT NULL,
    disponible   TINYINT(1)    NOT NULL DEFAULT 1,
    activo       TINYINT(1)    NOT NULL DEFAULT 1,
    id_categoria INT           NOT NULL,
    CONSTRAINT pk_producto           PRIMARY KEY (id_producto),
    CONSTRAINT uq_producto_codigo    UNIQUE (codigo),
    CONSTRAINT ck_producto_precio    CHECK (precio >= 0),
    CONSTRAINT fk_producto_categoria FOREIGN KEY (id_categoria)
        REFERENCES categoria (id_categoria)
) ENGINE = InnoDB;

CREATE TABLE combo (
    id_combo   INT           NOT NULL AUTO_INCREMENT,
    codigo     VARCHAR(10)   NOT NULL,
    nombre     VARCHAR(100)  NOT NULL,
    precio     DECIMAL(10,2) NOT NULL,
    disponible TINYINT(1)    NOT NULL DEFAULT 1,
    activo     TINYINT(1)    NOT NULL DEFAULT 1,
    CONSTRAINT pk_combo        PRIMARY KEY (id_combo),
    CONSTRAINT uq_combo_codigo UNIQUE (codigo),
    CONSTRAINT ck_combo_precio CHECK (precio >= 0)
) ENGINE = InnoDB;

-- Combo (N) ---- (M) Producto
CREATE TABLE combo_producto (
    id_combo    INT NOT NULL,
    id_producto INT NOT NULL,
    cantidad    INT NOT NULL DEFAULT 1,
    CONSTRAINT pk_combo_producto          PRIMARY KEY (id_combo, id_producto),
    CONSTRAINT ck_combo_producto_cantidad CHECK (cantidad > 0),
    CONSTRAINT fk_cp_combo    FOREIGN KEY (id_combo)    REFERENCES combo (id_combo)
        ON DELETE CASCADE,
    CONSTRAINT fk_cp_producto FOREIGN KEY (id_producto) REFERENCES producto (id_producto)
) ENGINE = InnoDB;

CREATE TABLE cliente (
    id_cliente       INT          NOT NULL AUTO_INCREMENT,
    dni              VARCHAR(10)  NOT NULL,
    nombre           VARCHAR(60)  NOT NULL,
    apellido         VARCHAR(60)  NOT NULL,
    telefono         VARCHAR(30)  NULL,
    email            VARCHAR(100) NULL,
    fecha_nacimiento DATE         NOT NULL,
    activo           TINYINT(1)   NOT NULL DEFAULT 1,
    CONSTRAINT pk_cliente     PRIMARY KEY (id_cliente),
    CONSTRAINT uq_cliente_dni UNIQUE (dni)
) ENGINE = InnoDB;

-- Cliente (1) ---- (N) Pedido (1) ---- (N) Detalle_pedido
CREATE TABLE pedido (
    id_pedido  INT           NOT NULL AUTO_INCREMENT,
    id_cliente INT           NOT NULL,
    fecha      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado     ENUM('PENDIENTE','CONFIRMADO','CANCELADO') NOT NULL DEFAULT 'PENDIENTE',
    total      DECIMAL(12,2) NOT NULL DEFAULT 0,
    CONSTRAINT pk_pedido         PRIMARY KEY (id_pedido),
    CONSTRAINT fk_pedido_cliente FOREIGN KEY (id_cliente)
        REFERENCES cliente (id_cliente)
) ENGINE = InnoDB;

-- Cada línea referencia a un producto O a un combo (nunca a ambos).
-- Se guarda el nombre y el precio vigentes al momento de la compra.
CREATE TABLE detalle_pedido (
    id_detalle      INT           NOT NULL AUTO_INCREMENT,
    id_pedido       INT           NOT NULL,
    tipo_articulo   ENUM('PRODUCTO','COMBO') NOT NULL,
    id_producto     INT           NULL,
    id_combo        INT           NULL,
    nombre_articulo VARCHAR(100)  NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    cantidad        INT           NOT NULL,
    CONSTRAINT pk_detalle          PRIMARY KEY (id_detalle),
    CONSTRAINT ck_detalle_cantidad CHECK (cantidad > 0),
    CONSTRAINT ck_detalle_articulo CHECK (
        (tipo_articulo = 'PRODUCTO' AND id_producto IS NOT NULL AND id_combo IS NULL) OR
        (tipo_articulo = 'COMBO'    AND id_combo    IS NOT NULL AND id_producto IS NULL)
    ),
    CONSTRAINT fk_detalle_pedido   FOREIGN KEY (id_pedido)   REFERENCES pedido (id_pedido),
    CONSTRAINT fk_detalle_producto FOREIGN KEY (id_producto) REFERENCES producto (id_producto),
    CONSTRAINT fk_detalle_combo    FOREIGN KEY (id_combo)    REFERENCES combo (id_combo)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
--  STORED PROCEDURES
-- ---------------------------------------------------------------------
DELIMITER $$

-- Crea la cabecera de un pedido para un cliente activo y devuelve su id.
CREATE PROCEDURE sp_crear_pedido(IN p_id_cliente INT, OUT p_id_pedido INT)
BEGIN
    DECLARE v_activo TINYINT DEFAULT NULL;

    SELECT activo INTO v_activo FROM cliente WHERE id_cliente = p_id_cliente;

    IF v_activo IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El cliente no existe';
    END IF;
    IF v_activo = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El cliente está dado de baja';
    END IF;

    INSERT INTO pedido (id_cliente) VALUES (p_id_cliente);
    SET p_id_pedido = LAST_INSERT_ID();
END$$

-- Agrega una línea al pedido tomando nombre y precio ACTUALES del catálogo.
-- p_tipo: 'PRODUCTO' o 'COMBO'
CREATE PROCEDURE sp_agregar_detalle(
    IN p_id_pedido   INT,
    IN p_tipo        VARCHAR(10),
    IN p_id_articulo INT,
    IN p_cantidad    INT)
BEGIN
    DECLARE v_nombre VARCHAR(100);
    DECLARE v_precio DECIMAL(10,2);
    DECLARE v_ok     TINYINT DEFAULT 0;

    IF p_tipo = 'PRODUCTO' THEN
        SELECT nombre, precio, 1 INTO v_nombre, v_precio, v_ok
          FROM producto
         WHERE id_producto = p_id_articulo AND disponible = 1 AND activo = 1;
    ELSEIF p_tipo = 'COMBO' THEN
        SELECT nombre, precio, 1 INTO v_nombre, v_precio, v_ok
          FROM combo
         WHERE id_combo = p_id_articulo AND disponible = 1 AND activo = 1;
    ELSE
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Tipo de artículo inválido';
    END IF;

    IF v_ok = 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'El artículo no existe o no está disponible';
    END IF;

    INSERT INTO detalle_pedido
        (id_pedido, tipo_articulo, id_producto, id_combo,
         nombre_articulo, precio_unitario, cantidad)
    VALUES
        (p_id_pedido, p_tipo,
         IF(p_tipo = 'PRODUCTO', p_id_articulo, NULL),
         IF(p_tipo = 'COMBO',    p_id_articulo, NULL),
         v_nombre, v_precio, p_cantidad);
END$$

-- Cambia el estado de un pedido (las transiciones las valida un trigger).
CREATE PROCEDURE sp_cambiar_estado_pedido(IN p_id_pedido INT, IN p_estado VARCHAR(12))
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pedido WHERE id_pedido = p_id_pedido) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'El pedido no existe';
    END IF;

    UPDATE pedido SET estado = p_estado WHERE id_pedido = p_id_pedido;
END$$

-- ---------------------------------------------------------------------
--  TRIGGERS
-- ---------------------------------------------------------------------

-- Solo se pueden agregar líneas a pedidos PENDIENTES.
CREATE TRIGGER trg_detalle_bi
BEFORE INSERT ON detalle_pedido
FOR EACH ROW
BEGIN
    DECLARE v_estado VARCHAR(12);
    SELECT estado INTO v_estado FROM pedido WHERE id_pedido = NEW.id_pedido;

    IF v_estado <> 'PENDIENTE' THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Solo se pueden agregar artículos a pedidos pendientes';
    END IF;
END$$

-- Mantiene actualizado el total del pedido al insertar una línea.
CREATE TRIGGER trg_detalle_ai
AFTER INSERT ON detalle_pedido
FOR EACH ROW
BEGIN
    UPDATE pedido
       SET total = (SELECT SUM(precio_unitario * cantidad)
                      FROM detalle_pedido
                     WHERE id_pedido = NEW.id_pedido)
     WHERE id_pedido = NEW.id_pedido;
END$$

-- Reglas de transición de estado:
--   PENDIENTE -> CONFIRMADO | CANCELADO     (un pedido vacío no se confirma)
--   CONFIRMADO / CANCELADO son estados finales.
CREATE TRIGGER trg_pedido_bu
BEFORE UPDATE ON pedido
FOR EACH ROW
BEGIN
    IF OLD.estado <> 'PENDIENTE' AND NEW.estado <> OLD.estado THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Solo se puede cambiar el estado de un pedido pendiente';
    END IF;

    IF NEW.estado = 'CONFIRMADO' AND OLD.estado = 'PENDIENTE' AND NEW.total <= 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'No se puede confirmar un pedido sin artículos';
    END IF;
END$$

-- Si una categoría se da de baja, sus productos dejan de estar disponibles.
CREATE TRIGGER trg_categoria_au
AFTER UPDATE ON categoria
FOR EACH ROW
BEGIN
    IF OLD.activo = 1 AND NEW.activo = 0 THEN
        UPDATE producto SET disponible = 0 WHERE id_categoria = NEW.id_categoria;
    END IF;
END$$

DELIMITER ;

-- ---------------------------------------------------------------------
--  DATOS DE PRUEBA (SEEDERS)
-- ---------------------------------------------------------------------

INSERT INTO categoria (nombre) VALUES
    ('Hamburguesas'),   -- 1
    ('Papas'),          -- 2
    ('Bebidas');        -- 3

INSERT INTO producto (codigo, nombre, precio, disponible, id_categoria) VALUES
    ('H001', 'Hamburguesa clásica',  5000.00, 1, 1),   -- 1
    ('H002', 'Hamburguesa doble',    7500.00, 1, 1),   -- 2
    ('H003', 'Hamburguesa triple',   9500.00, 1, 1),   -- 3
    ('P001', 'Papas chicas',         2000.00, 1, 2),   -- 4
    ('P002', 'Papas medianas',       2500.00, 1, 2),   -- 5
    ('P003', 'Papas grandes',        3000.00, 1, 2),   -- 6
    ('B001', 'Agua',                 1800.00, 1, 3),   -- 7
    ('B002', 'Gaseosa chica',        2200.00, 1, 3),   -- 8
    ('B003', 'Gaseosa mediana',      2500.00, 1, 3),   -- 9
    ('B004', 'Gaseosa grande',       2800.00, 1, 3);   -- 10

INSERT INTO combo (codigo, nombre, precio, disponible) VALUES
    ('C001', 'Combo hamburguesa simple chico',    8500.00, 1),    -- 1
    ('C002', 'Combo hamburguesa simple mediano',  9500.00, 1),    -- 2
    ('C003', 'Combo hamburguesa simple grande',  11000.00, 1);    -- 3

INSERT INTO combo_producto (id_combo, id_producto, cantidad) VALUES
    (1, 1, 1), (1, 4, 1), (1, 8,  1),
    (2, 1, 1), (2, 5, 1), (2, 9,  1),
    (3, 1, 1), (3, 6, 1), (3, 10, 1);

INSERT INTO cliente (dni, nombre, apellido, telefono, email, fecha_nacimiento) VALUES
    ('30123456', 'Lucía',   'Fernández', '11-5555-1001', 'lucia.fernandez@mail.com', '1992-03-14'),
    ('35234567', 'Martín',  'Gómez',     '11-5555-1002', 'martin.gomez@mail.com',    '1988-11-02'),
    ('40345678', 'Sofía',   'Rodríguez', '11-5555-1003', 'sofia.rodriguez@mail.com', '1999-07-21'),
    ('28456789', 'Diego',   'Pérez',     '11-5555-1004', NULL,                       '1985-01-30'),
    ('42567890', 'Camila',  'Álvarez',   NULL,           'camila.alvarez@mail.com',  '2001-09-09');

-- Pedidos de ejemplo creados con los mismos procedimientos que usa la aplicación
CALL sp_crear_pedido(1, @p1);
CALL sp_agregar_detalle(@p1, 'COMBO',    2, 1);
CALL sp_agregar_detalle(@p1, 'PRODUCTO', 5, 1);
CALL sp_cambiar_estado_pedido(@p1, 'CONFIRMADO');

CALL sp_crear_pedido(2, @p2);
CALL sp_agregar_detalle(@p2, 'PRODUCTO', 2, 2);
CALL sp_agregar_detalle(@p2, 'PRODUCTO', 6, 2);
CALL sp_agregar_detalle(@p2, 'PRODUCTO', 10, 2);
CALL sp_cambiar_estado_pedido(@p2, 'CONFIRMADO');

CALL sp_crear_pedido(3, @p3);
CALL sp_agregar_detalle(@p3, 'COMBO', 1, 3);
CALL sp_cambiar_estado_pedido(@p3, 'CANCELADO');

CALL sp_crear_pedido(4, @p4);          -- queda PENDIENTE
CALL sp_agregar_detalle(@p4, 'COMBO',    3, 1);
CALL sp_agregar_detalle(@p4, 'PRODUCTO', 7, 1);
