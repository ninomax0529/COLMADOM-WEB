-- =====================================================================
-- LOTES DE INVENTARIO - FASE 1 (estructura)
-- Las existencias se manejan en UNIDAD BASE (igual que articulo_almacen).
-- Invariante: para un artículo con controla_lote = 1,
--   articulo_almacen.existencia = SUM(lote_almacen.existencia) de ese almacén.
-- =====================================================================

-- 1. Bandera en la ficha del artículo
ALTER TABLE articulo
  ADD COLUMN controla_lote bit(1) NOT NULL DEFAULT b'0' AFTER perecedero;

UPDATE articulo SET controla_lote = perecedero;

-- 2. Lote: un número de lote de un artículo, con su vencimiento y costo de entrada
CREATE TABLE lote (
  codigo int NOT NULL AUTO_INCREMENT,
  articulo int NOT NULL,
  numero_lote varchar(50) NOT NULL,
  fecha_vencimiento date DEFAULT NULL,
  fecha_entrada datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  costo_unitario decimal(12,4) DEFAULT NULL COMMENT 'costo por unidad base al entrar',
  proveedor int DEFAULT NULL,
  documento_origen varchar(60) DEFAULT NULL COMMENT 'Ej: REC-15, ENT-8, INICIAL',
  activo bit(1) NOT NULL DEFAULT b'1',
  creado_por varchar(50) DEFAULT NULL,
  PRIMARY KEY (codigo),
  UNIQUE KEY uk_lote_articulo_numero (articulo, numero_lote),
  KEY idx_lote_articulo_vence (articulo, fecha_vencimiento),
  KEY idx_lote_vence (fecha_vencimiento),
  CONSTRAINT fk_lote_articulo FOREIGN KEY (articulo) REFERENCES articulo (codigo),
  CONSTRAINT fk_lote_proveedor FOREIGN KEY (proveedor) REFERENCES proveedor (codigo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Existencia de cada lote en cada almacén (unidad base)
CREATE TABLE lote_almacen (
  codigo int NOT NULL AUTO_INCREMENT,
  lote int NOT NULL,
  almacen int NOT NULL,
  existencia decimal(12,2) NOT NULL DEFAULT 0,
  fecha_actualizacion timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (codigo),
  UNIQUE KEY uk_lote_almacen (lote, almacen),
  KEY idx_lote_almacen_existencia (almacen, existencia),
  CONSTRAINT fk_lote_almacen_lote FOREIGN KEY (lote) REFERENCES lote (codigo),
  CONSTRAINT fk_lote_almacen_almacen FOREIGN KEY (almacen) REFERENCES almacen (codigo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Kardex: lote del movimiento y existencia del lote antes/después
ALTER TABLE movimiento_inventario
  ADD COLUMN lote int DEFAULT NULL,
  ADD COLUMN existencia_lote_anterior decimal(12,2) DEFAULT NULL,
  ADD COLUMN existencia_lote_nueva decimal(12,2) DEFAULT NULL,
  ADD KEY idx_mov_inv_lote (lote),
  ADD CONSTRAINT fk_mov_inv_lote FOREIGN KEY (lote) REFERENCES lote (codigo);

-- 5. Lote en los detalles de documentos de inventario
--    (numero_lote / fecha_vencimiento: lo que digita el usuario en documentos que crean lotes)
ALTER TABLE detalle_recepcion_mercancia
  ADD COLUMN lote int DEFAULT NULL,
  ADD COLUMN numero_lote varchar(50) DEFAULT NULL,
  ADD COLUMN fecha_vencimiento date DEFAULT NULL,
  ADD KEY idx_det_rec_lote (lote),
  ADD CONSTRAINT fk_det_rec_lote FOREIGN KEY (lote) REFERENCES lote (codigo);

ALTER TABLE detalle_entrada_inventario
  ADD COLUMN lote int DEFAULT NULL,
  ADD COLUMN numero_lote varchar(50) DEFAULT NULL,
  ADD COLUMN fecha_vencimiento date DEFAULT NULL,
  ADD KEY idx_det_ent_lote (lote),
  ADD CONSTRAINT fk_det_ent_lote FOREIGN KEY (lote) REFERENCES lote (codigo);

ALTER TABLE detalle_ajuste_inventario
  ADD COLUMN lote int DEFAULT NULL,
  ADD COLUMN numero_lote varchar(50) DEFAULT NULL,
  ADD COLUMN fecha_vencimiento date DEFAULT NULL,
  ADD KEY idx_det_aju_lote (lote),
  ADD CONSTRAINT fk_det_aju_lote FOREIGN KEY (lote) REFERENCES lote (codigo);

ALTER TABLE detalle_salida_inventario
  ADD COLUMN lote int DEFAULT NULL,
  ADD KEY idx_det_sal_lote (lote),
  ADD CONSTRAINT fk_det_sal_lote FOREIGN KEY (lote) REFERENCES lote (codigo);

ALTER TABLE detalle_merma
  ADD COLUMN lote int DEFAULT NULL,
  ADD KEY idx_det_mer_lote (lote),
  ADD CONSTRAINT fk_det_mer_lote FOREIGN KEY (lote) REFERENCES lote (codigo);

ALTER TABLE detalle_traslado_inventario
  ADD COLUMN lote int DEFAULT NULL,
  ADD KEY idx_det_tra_lote (lote),
  ADD CONSTRAINT fk_det_tra_lote FOREIGN KEY (lote) REFERENCES lote (codigo);

-- 6. De qué lote(s) salió cada línea de venta (una línea puede consumir varios lotes).
--    Sirve para que la anulación y la devolución repongan en el mismo lote.
CREATE TABLE detalle_factura_venta_lote (
  codigo int NOT NULL AUTO_INCREMENT,
  detalle_factura_de_venta int NOT NULL,
  lote int NOT NULL,
  almacen int NOT NULL,
  cantidad decimal(12,2) NOT NULL COMMENT 'unidad base',
  cantidad_devuelta decimal(12,2) NOT NULL DEFAULT 0 COMMENT 'unidad base ya repuesta por devolución/anulación',
  lote_vencido bit(1) NOT NULL DEFAULT b'0' COMMENT 'se vendió con el lote vencido',
  autorizado_por varchar(50) DEFAULT NULL COMMENT 'supervisor que autorizó vender el lote vencido',
  fecha datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (codigo),
  KEY idx_dfvl_detalle (detalle_factura_de_venta),
  KEY idx_dfvl_lote (lote),
  CONSTRAINT fk_dfvl_detalle FOREIGN KEY (detalle_factura_de_venta) REFERENCES detalle_factura_de_venta (codigo),
  CONSTRAINT fk_dfvl_lote FOREIGN KEY (lote) REFERENCES lote (codigo),
  CONSTRAINT fk_dfvl_almacen FOREIGN KEY (almacen) REFERENCES almacen (codigo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Lote INICIAL con la existencia actual de los artículos que ya controlan lote
INSERT INTO lote (articulo, numero_lote, fecha_vencimiento, fecha_entrada, costo_unitario, documento_origen, creado_por)
SELECT a.codigo, 'INICIAL', NULL, NOW(), a.precio_compra, 'INICIAL', 'SISTEMA'
FROM articulo a
WHERE a.controla_lote = 1
  AND EXISTS (SELECT 1 FROM articulo_almacen aa WHERE aa.articulo = a.codigo AND aa.existencia > 0);

INSERT INTO lote_almacen (lote, almacen, existencia)
SELECT l.codigo, aa.almacen, aa.existencia
FROM lote l
JOIN articulo_almacen aa ON aa.articulo = l.articulo AND aa.existencia > 0
WHERE l.numero_lote = 'INICIAL';

-- 8. Chequeo de integridad (correr cuando se quiera; debe devolver 0 filas)
-- SELECT aa.articulo, aa.almacen, aa.existencia, COALESCE(SUM(la.existencia),0) AS suma_lotes
-- FROM articulo_almacen aa
-- JOIN articulo a ON a.codigo = aa.articulo AND a.controla_lote = 1
-- LEFT JOIN lote l ON l.articulo = aa.articulo
-- LEFT JOIN lote_almacen la ON la.lote = l.codigo AND la.almacen = aa.almacen
-- GROUP BY aa.articulo, aa.almacen, aa.existencia
-- HAVING aa.existencia <> COALESCE(SUM(la.existencia),0);
