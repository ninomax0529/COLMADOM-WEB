-- =====================================================================
-- LIMPIEZA DE DATOS DE PRUEBA - db_colmadom
-- ---------------------------------------------------------------------
-- Borra TODOS los movimientos (ventas, compras, inventario, caja, CxC,
-- CxP, delivery, lotes) y deja los catálogos y maestros:
--   artículos, empaques, clientes, proveedores, almacenes, unidades,
--   usuarios, roles, tipos, estados, plazos, delivery/vehículos, empresa.
--
-- Las existencias y los balances quedan en 0 y los contadores
-- (AUTO_INCREMENT) de los documentos empiezan otra vez en 1.
--
-- ANTES DE CORRERLO:
--   1. Detén la aplicación.
--   2. Haz un respaldo:
--      mysqldump -h localhost -P 3307 -u root -p db_colmadom > respaldo-antes-limpieza.sql
--
-- TRUNCATE no se puede deshacer (no entra en transacción).
-- =====================================================================

USE db_colmadom;

SET FOREIGN_KEY_CHECKS = 0;

-- ---------- Ventas, CxC y delivery ----------
TRUNCATE TABLE detalle_factura_venta_lote;
TRUNCATE TABLE detalle_factura_de_venta;
TRUNCATE TABLE detalle_recibo_de_ingreso;
TRUNCATE TABLE recibo_de_ingreso;
TRUNCATE TABLE cuenta_por_cobrar;
TRUNCATE TABLE historial_limite_credito;
TRUNCATE TABLE detalle_liquidacion_delivery;
TRUNCATE TABLE cuenta_motorista;
TRUNCATE TABLE liquidacion_delivery;
TRUNCATE TABLE factura_de_venta;

-- ---------- Compras y CxP ----------
TRUNCATE TABLE aplicacion_credito_proveedor;
TRUNCATE TABLE detalle_devolucion_proveedor;
TRUNCATE TABLE devolucion_proveedor;
TRUNCATE TABLE detalle_recibo_de_egreso;
TRUNCATE TABLE recibo_de_egreso;
TRUNCATE TABLE detalle_factura_de_compra;
TRUNCATE TABLE factura_de_compra;
TRUNCATE TABLE detalle_recepcion_mercancia;
TRUNCATE TABLE recepcion_mercancia;
TRUNCATE TABLE detalle_ordende_de_compra;
TRUNCATE TABLE orden_de_compra;

-- ---------- Inventario ----------
TRUNCATE TABLE detalle_entrada_inventario;
TRUNCATE TABLE entrada_inventario;
TRUNCATE TABLE detalle_merma;
TRUNCATE TABLE merma;
TRUNCATE TABLE detalle_salida_inventario;
TRUNCATE TABLE salida_inventario;
TRUNCATE TABLE detalle_ajuste_inventario;
TRUNCATE TABLE ajuste_inventario;
TRUNCATE TABLE detalle_traslado_inventario;
TRUNCATE TABLE traslado_inventario;
TRUNCATE TABLE movimiento_inventario;

-- ---------- Lotes (SIN-LOTE e INICIAL se crean solos cuando hacen falta) ----------
TRUNCATE TABLE lote_almacen;
TRUNCATE TABLE lote;

-- ---------- Caja ----------
TRUNCATE TABLE movimientos_caja;
TRUNCATE TABLE desglose_caja;
TRUNCATE TABLE caja_turno;
TRUNCATE TABLE caja_chica;

-- ---------- Tablas de respaldo viejas ----------
DROP TABLE IF EXISTS bk_20261106_articulo;
DROP TABLE IF EXISTS bk_20261106_cliente;
DROP TABLE IF EXISTS bk_20261106_cuenta_por_cobrar;
DROP TABLE IF EXISTS bk_20261106_factura_de_compra;
DROP TABLE IF EXISTS bk_20261106_factura_de_venta;
DROP TABLE IF EXISTS bk_20261106_recibo_de_egreso;
DROP TABLE IF EXISTS bk_20261106_recibo_de_ingreso;

SET FOREIGN_KEY_CHECKS = 1;

-- ---------- Existencias y balances en cero ----------
-- (SQL_SAFE_UPDATES por si se corre desde MySQL Workbench)
SET SQL_SAFE_UPDATES = 0;
UPDATE articulo_almacen SET existencia = 0;
UPDATE articulo         SET existencia = 0;
UPDATE cliente          SET balance = 0;
SET SQL_SAFE_UPDATES = 1;

-- =====================================================================
-- VERIFICACIÓN: todo debe salir en 0
-- =====================================================================
SELECT 'facturas venta'     AS tabla, COUNT(*) AS filas FROM factura_de_venta
UNION ALL SELECT 'facturas compra',   COUNT(*) FROM factura_de_compra
UNION ALL SELECT 'recepciones',       COUNT(*) FROM recepcion_mercancia
UNION ALL SELECT 'kardex',            COUNT(*) FROM movimiento_inventario
UNION ALL SELECT 'lotes',             COUNT(*) FROM lote
UNION ALL SELECT 'cuentas x cobrar',  COUNT(*) FROM cuenta_por_cobrar
UNION ALL SELECT 'caja turnos',       COUNT(*) FROM caja_turno
UNION ALL SELECT 'existencia <> 0',   COUNT(*) FROM articulo_almacen WHERE existencia <> 0
UNION ALL SELECT 'clientes con balance', COUNT(*) FROM cliente WHERE balance <> 0;

-- Maestros que se conservan (deben tener filas)
SELECT 'articulos' AS maestro, COUNT(*) AS filas FROM articulo
UNION ALL SELECT 'empaques',    COUNT(*) FROM articulo_empaque
UNION ALL SELECT 'clientes',    COUNT(*) FROM cliente
UNION ALL SELECT 'proveedores', COUNT(*) FROM proveedor
UNION ALL SELECT 'almacenes',   COUNT(*) FROM almacen;
