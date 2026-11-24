-- articulo.fecha_creacion tenía ON UPDATE CURRENT_TIMESTAMP: cambiaba cada vez
-- que se editaba el artículo, así que en realidad era la fecha de
-- actualización. Se renombra a fecha_actualizacion (conserva su comportamiento)
-- y se crea una fecha_creacion de verdad, que no cambia al editar. La usa la
-- clasificación ABC para saber si un artículo es nuevo.
ALTER TABLE articulo
    RENAME COLUMN fecha_creacion TO fecha_actualizacion;

ALTER TABLE articulo
    MODIFY fecha_actualizacion TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

ALTER TABLE articulo
    ADD COLUMN fecha_creacion DATETIME NULL DEFAULT CURRENT_TIMESTAMP AFTER creado_por;

-- Artículos que ya existen: la fecha más antigua que se conoce de cada uno
-- (su fecha guardada, su primer movimiento de inventario o su primera venta).
-- fecha_actualizacion se asigna a sí misma para que el ON UPDATE no la cambie.
UPDATE articulo a
LEFT JOIN (SELECT articulo, MIN(fecha_movimiento) AS primero
           FROM movimiento_inventario GROUP BY articulo) m ON m.articulo = a.codigo
LEFT JOIN (SELECT d.articulo, MIN(f.fecha) AS primero
           FROM detalle_factura_de_venta d
           JOIN factura_de_venta f ON f.codigo = d.factura
           GROUP BY d.articulo) v ON v.articulo = a.codigo
SET a.fecha_creacion = LEAST(
        COALESCE(a.fecha_actualizacion, m.primero, v.primero, NOW()),
        COALESCE(m.primero, a.fecha_actualizacion, v.primero, NOW()),
        COALESCE(v.primero, a.fecha_actualizacion, m.primero, NOW())),
    a.fecha_actualizacion = a.fecha_actualizacion;

ALTER TABLE articulo
    MODIFY fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP;
