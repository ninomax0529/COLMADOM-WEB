-- =====================================================================
-- Devoluciones de venta: la factura registra cuánto se ha devuelto y el
-- balance pendiente lo descuenta (antes: total - pagos, sin devoluciones).
-- =====================================================================

ALTER TABLE factura_de_venta
  ADD COLUMN total_devuelto decimal(12,2) NOT NULL DEFAULT 0.00 AFTER total_pendiente;

-- @@ función (se ejecuta entera)
DROP FUNCTION IF EXISTS BALANCE_PENDIENTE_FACT_VENTA;

-- @@
CREATE FUNCTION BALANCE_PENDIENTE_FACT_VENTA(fact int) RETURNS double(10,2)
    READS SQL DATA
BEGIN
    DECLARE total_fact double(10,2);
    DECLARE total_dev double(10,2);
    DECLARE total_pago double(10,2);

    SET total_pago = PAGO_FACT_VENTA(fact);

    SELECT total, COALESCE(total_devuelto, 0) INTO total_fact, total_dev
    FROM factura_de_venta
    WHERE anulada = false AND codigo = fact;

    RETURN ROUND(total_fact - total_dev - total_pago, 2);
END
