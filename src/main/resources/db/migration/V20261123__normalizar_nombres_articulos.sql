-- Nombres de productos uniformes: MAYÚSCULAS, sin espacios al inicio/final
-- ni repetidos ("leche rica  lista " -> "LECHE RICA LISTA"). Desde ahora
-- ArticuloServiceImpl.normalizarNombre lo hace al guardar el artículo.
UPDATE articulo
SET descripcion = UPPER(TRIM(REGEXP_REPLACE(descripcion, '[[:space:]]+', ' ')))
WHERE descripcion IS NOT NULL;

-- Copia del nombre en cada presentación (la que muestra el POS)
UPDATE articulo_empaque
SET nombre_articulo = UPPER(TRIM(REGEXP_REPLACE(nombre_articulo, '[[:space:]]+', ' ')))
WHERE nombre_articulo IS NOT NULL;
