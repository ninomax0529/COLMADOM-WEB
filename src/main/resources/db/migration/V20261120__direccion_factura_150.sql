-- Dirección de entrega del delivery: 45 caracteres no alcanzaban para
-- "calle, número, sector y referencia". Se amplía a 150.
ALTER TABLE factura_de_venta MODIFY direccion VARCHAR(150) NULL;
