-- =====================================================================
-- Liquidación de delivery: pagos mixtos, anulación y cuenta del motorista
--
-- 1. Pagos mixtos: el motorista puede entregar parte en transferencia.
-- 2. Anulación: la liquidación queda en el historial marcada como anulada y
--    sus facturas vuelven a quedar pendientes de liquidar. Para poder volver
--    a liquidar la misma factura, la unicidad de la factura se aplica solo a
--    las líneas NO anuladas (columna generada factura_activa).
-- 3. Cuenta del motorista: los faltantes se le cargan como deuda y sus
--    abonos la rebajan (monto positivo = debe más, negativo = debe menos).
-- =====================================================================

ALTER TABLE liquidacion_delivery
  ADD COLUMN transferencia_recibida decimal(18,2) NOT NULL DEFAULT 0.00 AFTER efectivo_recibido,
  ADD COLUMN referencia_transferencia varchar(100) NULL AFTER transferencia_recibida,
  ADD COLUMN faltante_cargado decimal(18,2) NOT NULL DEFAULT 0.00 AFTER diferencia,
  ADD COLUMN anulada tinyint(1) NOT NULL DEFAULT 0,
  ADD COLUMN fecha_anulacion datetime NULL,
  ADD COLUMN motivo_anulacion varchar(300) NULL,
  ADD COLUMN usuario_anulacion varchar(100) NULL;

ALTER TABLE detalle_liquidacion_delivery
  ADD COLUMN anulada tinyint(1) NOT NULL DEFAULT 0,
  ADD COLUMN factura_activa int GENERATED ALWAYS AS (IF(anulada = 0, factura, NULL)) STORED,
  ADD KEY idx_detalle_liq_factura (factura);

ALTER TABLE detalle_liquidacion_delivery
  DROP INDEX uk_detalle_liq_factura,
  ADD UNIQUE KEY uk_detalle_liq_factura_activa (factura_activa);

CREATE TABLE cuenta_motorista (
  codigo int NOT NULL AUTO_INCREMENT,
  delivery int NOT NULL,
  fecha datetime NOT NULL,
  tipo varchar(30) NOT NULL,
  monto decimal(18,2) NOT NULL,
  liquidacion_delivery int NULL,
  descripcion varchar(300) NULL,
  nombre_usuario varchar(100) NULL,
  PRIMARY KEY (codigo),
  KEY idx_cuenta_motorista_delivery (delivery, fecha),
  KEY idx_cuenta_motorista_liq (liquidacion_delivery),
  CONSTRAINT fk_cuenta_motorista_delivery FOREIGN KEY (delivery) REFERENCES delivery (codigo),
  CONSTRAINT fk_cuenta_motorista_liq FOREIGN KEY (liquidacion_delivery) REFERENCES liquidacion_delivery (codigo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
