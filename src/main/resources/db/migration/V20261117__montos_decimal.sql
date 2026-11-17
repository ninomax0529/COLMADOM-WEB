-- 2026-11-17: montos y tasas que estaban en DOUBLE pasan a DECIMAL
-- (las entidades Java ahora usan BigDecimal). Evita errores de centavos.
-- Hacer respaldo antes de ejecutar.

ALTER TABLE articulo
    MODIFY `precio_venta_con_itbis` DECIMAL(12,2) DEFAULT NULL,
    MODIFY `precio_venta_anterior`  DECIMAL(12,2) DEFAULT NULL,
    MODIFY `margen_beneficio`       DECIMAL(12,4) DEFAULT NULL,
    MODIFY `porciento_utilidad`     DECIMAL(9,4)  DEFAULT NULL,
    MODIFY `itbis_gravado`          DECIMAL(5,2)  DEFAULT NULL;

ALTER TABLE caja_chica
    MODIFY `monto_apertura`        DECIMAL(12,2) NOT NULL,
    MODIFY `monto_cierre`          DECIMAL(12,2) DEFAULT NULL,
    MODIFY `monto_cierre_esperado` DECIMAL(12,2) DEFAULT NULL,
    MODIFY `monto_cierre_real`     DECIMAL(12,2) DEFAULT NULL;

ALTER TABLE proveedor
    MODIFY `limite_credito` DECIMAL(12,2) DEFAULT NULL;
