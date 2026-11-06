-- =====================================================================
-- Reparación de integridad de datos (2026-11-06)
-- Alinea pagos, balances y existencias con los documentos vigentes.
-- Crea respaldos bk_20261106_* antes de modificar.
-- =====================================================================

-- 0. Respaldos
CREATE TABLE bk_20261106_recibo_de_ingreso AS SELECT * FROM recibo_de_ingreso;
CREATE TABLE bk_20261106_recibo_de_egreso AS SELECT * FROM recibo_de_egreso;
CREATE TABLE bk_20261106_factura_de_venta AS SELECT * FROM factura_de_venta;
CREATE TABLE bk_20261106_factura_de_compra AS SELECT * FROM factura_de_compra;
CREATE TABLE bk_20261106_articulo AS SELECT codigo, existencia FROM articulo;
CREATE TABLE bk_20261106_cuenta_por_cobrar AS SELECT * FROM cuenta_por_cobrar;
CREATE TABLE bk_20261106_cliente AS SELECT codigo, balance FROM cliente;

-- 1. Recibos sin estado (anulado NULL): el sistema ya los ignoraba en
--    PAGO_FACT_VENTA / PAGO_FACT_COMPRA. Se marcan anulados y la columna
--    deja de aceptar NULL para que no vuelva a pasar.
UPDATE recibo_de_ingreso
SET anulado = 1,
    concepto = CONCAT(COALESCE(concepto, ''), ' | ANULADO (reparación 2026-11-06: registro sin estado)')
WHERE anulado IS NULL;

UPDATE recibo_de_egreso
SET anulado = 1,
    concepto = CONCAT(COALESCE(concepto, ''), ' | ANULADO (reparación 2026-11-06: registro sin estado)')
WHERE anulado IS NULL;

ALTER TABLE recibo_de_ingreso MODIFY COLUMN anulado tinyint(1) NOT NULL DEFAULT 0;
ALTER TABLE recibo_de_egreso MODIFY COLUMN anulado tinyint(1) NOT NULL DEFAULT 0;

-- 2. Recibos que la liquidación de delivery creó por error sobre facturas
--    FIADAS (el cliente no pagó nada; el motorista no cobra el fiado).
UPDATE recibo_de_ingreso r
SET r.anulado = 1,
    r.concepto = CONCAT(r.concepto, ' | ANULADO (reparación 2026-11-06: la liquidación no cobra facturas fiadas)')
WHERE r.anulado = 0
  AND r.concepto = 'Pago con liquidacion de factura'
  AND EXISTS (SELECT 1 FROM detalle_recibo_de_ingreso d
              JOIN factura_de_venta f ON f.codigo = d.factura
              WHERE d.recibo_ingreso = r.codigo AND f.tipo_venta = 2);

-- 2b. Facturas de delivery de CONTADO cobradas dos veces: una versión anterior
--     del POS les creó un recibo automático al vender y luego la liquidación
--     creó el suyo. El cobro real es el de la liquidación.
UPDATE recibo_de_ingreso r
SET r.anulado = 1,
    r.concepto = CONCAT(r.concepto, ' | ANULADO (reparación 2026-11-06: duplicado de la liquidación de delivery)')
WHERE r.anulado = 0
  AND r.concepto LIKE 'Recibo autom%tica por Factura de venta%'
  AND EXISTS (SELECT 1 FROM detalle_recibo_de_ingreso d
              JOIN factura_de_venta f ON f.codigo = d.factura
              WHERE d.recibo_ingreso = r.codigo AND f.delivery IS NOT NULL AND f.tipo_venta = 1
                AND EXISTS (SELECT 1 FROM detalle_recibo_de_ingreso d2
                            JOIN recibo_de_ingreso r2 ON r2.codigo = d2.recibo_ingreso
                            WHERE d2.factura = f.codigo AND r2.anulado = 0
                              AND r2.concepto = 'Pago con liquidacion de factura'));

-- 3. Facturas de venta vigentes: pagado y pendiente según sus recibos vigentes
UPDATE factura_de_venta f
SET f.total_pagado = PAGO_FACT_VENTA(f.codigo),
    f.total_pendiente = f.total - PAGO_FACT_VENTA(f.codigo)
WHERE COALESCE(f.anulada, 0) = 0
  AND (f.tipo_venta = 2 OR f.delivery IS NOT NULL OR PAGO_FACT_VENTA(f.codigo) > 0);

-- 4. Facturas de compra vigentes con recibos o a crédito: pagado y pendiente
--    según sus recibos de egreso vigentes. (Las de contado sin recibo se dejan.)
UPDATE factura_de_compra f
SET f.total_pagado = PAGO_FACT_COMPRA(f.codigo),
    f.total_pendiente = f.total - PAGO_FACT_COMPRA(f.codigo)
WHERE COALESCE(f.anulada, 0) = 0
  AND (PAGO_FACT_COMPRA(f.codigo) > 0 OR f.condicion_pago = 2);

-- 5. Existencia general del artículo = suma de sus almacenes
UPDATE articulo a
SET a.existencia = COALESCE((SELECT SUM(x.existencia) FROM articulo_almacen x WHERE x.articulo = a.codigo), 0);

-- 6. Reconstruir el estado de cuenta (solo tenía la carga inicial) con los
--    recibos vigentes ya depurados
DELETE FROM cuenta_por_cobrar WHERE usuario = 'MIGRACION';

INSERT INTO cuenta_por_cobrar
  (cliente, fecha, tipo_movimiento, factura, recibo_ingreso, numero_documento,
   concepto, debito, credito, balance_anterior, balance_nuevo, usuario)
SELECT m.cliente, m.fecha, m.tipo, m.factura, m.recibo, m.numdoc, m.concepto, m.debito, m.credito,
       SUM(m.debito - m.credito) OVER w - (m.debito - m.credito),
       SUM(m.debito - m.credito) OVER w,
       'MIGRACION'
FROM (
    SELECT f.cliente, COALESCE(f.fecha_creacion, f.fecha) AS fecha, 0 AS orden, f.codigo AS id,
           'FACTURA' AS tipo, f.codigo AS factura, NULL AS recibo,
           CONCAT('FAC-', f.codigo) AS numdoc,
           CONVERT(CONCAT('Factura a crédito #', f.codigo) USING utf8mb4) AS concepto,
           COALESCE(f.total, 0) AS debito, 0 AS credito
    FROM factura_de_venta f
    WHERE f.tipo_venta = 2 AND COALESCE(f.anulada, 0) = 0 AND f.cliente IS NOT NULL
    UNION ALL
    SELECT f.cliente, COALESCE(r.fecha_creacion, r.fecha), 1, d.codigo,
           'RECIBO', f.codigo, r.codigo, CONCAT('RI-', r.codigo),
           CONVERT(LEFT(CONCAT('Pago recibo #', r.codigo, ' a factura #', f.codigo), 255) USING utf8mb4),
           0, COALESCE(d.total, 0)
    FROM detalle_recibo_de_ingreso d
    JOIN recibo_de_ingreso r ON r.codigo = d.recibo_ingreso
    JOIN factura_de_venta f ON f.codigo = d.factura
    WHERE f.tipo_venta = 2 AND COALESCE(f.anulada, 0) = 0 AND f.cliente IS NOT NULL
      AND r.anulado = 0
) m
WINDOW w AS (PARTITION BY m.cliente ORDER BY m.fecha, m.orden, m.id ROWS UNBOUNDED PRECEDING);

UPDATE cliente c
SET c.balance = COALESCE((SELECT SUM(x.debito - x.credito) FROM cuenta_por_cobrar x WHERE x.cliente = c.codigo), 0);
