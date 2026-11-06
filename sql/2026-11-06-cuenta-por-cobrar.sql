-- =====================================================================
-- Cuenta por cobrar (estado de cuenta del cliente) y límite de crédito
-- Fecha: 2026-11-06
-- Ejecutar UNA sola vez sobre db_colmadom (hacer respaldo antes).
-- =====================================================================

-- 1. Cliente: balance actual (caché del estado de cuenta) y límite en decimal
--    limite_credito NULL = sin límite definido (no se valida)
--    limite_credito 0    = no se le fía
ALTER TABLE `cliente`
  MODIFY COLUMN `limite_credito` decimal(12,2) DEFAULT NULL,
  ADD COLUMN `balance` decimal(12,2) NOT NULL DEFAULT 0.00 AFTER `limite_credito`;

-- 2. Estado de cuenta: cada factura a crédito (débito) y cada pago (crédito)
CREATE TABLE IF NOT EXISTS `cuenta_por_cobrar` (
  `codigo` int NOT NULL AUTO_INCREMENT,
  `cliente` int NOT NULL,
  `fecha` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `tipo_movimiento` varchar(30) NOT NULL COMMENT 'FACTURA, RECIBO, ANULACION_FACTURA, DEVOLUCION, AJUSTE_DEBITO, AJUSTE_CREDITO',
  `factura` int DEFAULT NULL,
  `recibo_ingreso` int DEFAULT NULL,
  `numero_documento` varchar(50) DEFAULT NULL,
  `concepto` varchar(255) DEFAULT NULL,
  `debito` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT 'Aumenta lo que debe el cliente',
  `credito` decimal(12,2) NOT NULL DEFAULT 0.00 COMMENT 'Disminuye lo que debe el cliente',
  `balance_anterior` decimal(12,2) NOT NULL DEFAULT 0.00,
  `balance_nuevo` decimal(12,2) NOT NULL DEFAULT 0.00,
  `usuario` varchar(100) DEFAULT NULL,
  `fecha_creacion` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`codigo`),
  KEY `idx_cxc_cliente_fecha` (`cliente`, `fecha`),
  KEY `idx_cxc_factura` (`factura`),
  KEY `idx_cxc_recibo` (`recibo_ingreso`),
  CONSTRAINT `fk_cxc_cliente` FOREIGN KEY (`cliente`) REFERENCES `cliente` (`codigo`),
  CONSTRAINT `fk_cxc_factura` FOREIGN KEY (`factura`) REFERENCES `factura_de_venta` (`codigo`),
  CONSTRAINT `fk_cxc_recibo` FOREIGN KEY (`recibo_ingreso`) REFERENCES `recibo_de_ingreso` (`codigo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 3. Histórico de cambios del límite de crédito
CREATE TABLE IF NOT EXISTS `historial_limite_credito` (
  `codigo` int NOT NULL AUTO_INCREMENT,
  `cliente` int NOT NULL,
  `limite_anterior` decimal(12,2) DEFAULT NULL,
  `limite_nuevo` decimal(12,2) DEFAULT NULL,
  `motivo` varchar(255) DEFAULT NULL,
  `usuario` varchar(100) DEFAULT NULL,
  `fecha` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`codigo`),
  KEY `idx_hist_limite_cliente` (`cliente`, `fecha`),
  CONSTRAINT `fk_hist_limite_cliente` FOREIGN KEY (`cliente`) REFERENCES `cliente` (`codigo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 4. Carga inicial desde el histórico
--    Débitos : facturas a crédito (tipo_venta = 2) no anuladas.
--    Créditos: líneas de recibos no anulados aplicadas a esas facturas.
--    Se excluyen los recibos "Pago con liquidacion de factura" sobre facturas
--    a crédito: los generó por error la liquidación de delivery (ya corregido)
--    y el cliente nunca pagó ese dinero.
INSERT INTO `cuenta_por_cobrar`
  (`cliente`, `fecha`, `tipo_movimiento`, `factura`, `recibo_ingreso`, `numero_documento`,
   `concepto`, `debito`, `credito`, `balance_anterior`, `balance_nuevo`, `usuario`)
SELECT m.cliente, m.fecha, m.tipo, m.factura, m.recibo, m.numdoc, m.concepto, m.debito, m.credito,
       SUM(m.debito - m.credito) OVER w - (m.debito - m.credito),
       SUM(m.debito - m.credito) OVER w,
       'MIGRACION'
FROM (
    SELECT f.cliente,
           COALESCE(f.fecha_creacion, f.fecha) AS fecha,
           0 AS orden, f.codigo AS id,
           'FACTURA' AS tipo, f.codigo AS factura, NULL AS recibo,
           CONCAT('FAC-', f.codigo) AS numdoc,
           CONVERT(CONCAT('Factura a crédito #', f.codigo) USING utf8mb4) AS concepto,
           COALESCE(f.total, 0) AS debito, 0 AS credito
    FROM factura_de_venta f
    WHERE f.tipo_venta = 2 AND COALESCE(f.anulada, 0) = 0 AND f.cliente IS NOT NULL
    UNION ALL
    SELECT f.cliente,
           COALESCE(r.fecha_creacion, r.fecha) AS fecha,
           1 AS orden, d.codigo AS id,
           'RECIBO', f.codigo, r.codigo,
           CONCAT('RI-', r.codigo),
           CONVERT(LEFT(CONCAT('Pago recibo #', r.codigo, ' a factura #', f.codigo), 255) USING utf8mb4),
           0, COALESCE(d.total, 0)
    FROM detalle_recibo_de_ingreso d
    JOIN recibo_de_ingreso r ON r.codigo = d.recibo_ingreso
    JOIN factura_de_venta f ON f.codigo = d.factura
    WHERE f.tipo_venta = 2 AND COALESCE(f.anulada, 0) = 0 AND f.cliente IS NOT NULL
      AND COALESCE(r.anulado, 0) = 0
      AND COALESCE(r.concepto, '') <> 'Pago con liquidacion de factura'
) m
WINDOW w AS (PARTITION BY m.cliente ORDER BY m.fecha, m.orden, m.id ROWS UNBOUNDED PRECEDING);

-- 5. Balance de cada cliente = suma de su estado de cuenta
UPDATE `cliente` c
SET c.`balance` = COALESCE((SELECT SUM(x.debito - x.credito)
                            FROM cuenta_por_cobrar x
                            WHERE x.cliente = c.codigo), 0);
