-- Segunda fase del ABC, paso 2: mínimo y máximo sugeridos por artículo.
--   mínimo = demanda diaria x (días de reposición + días de seguridad de la clase)
--   máximo = mínimo + demanda diaria x días de compra de la clase
-- Son sugerencias: se copian a articulo.minimo / maximo solo con
-- "Aplicar sugeridos" en la pantalla Clasificación ABC.
ALTER TABLE articulo_clasificacion_abc
    ADD COLUMN minimo_sugerido DECIMAL(12,2) NULL,
    ADD COLUMN maximo_sugerido DECIMAL(12,2) NULL;

-- Parámetros (días). Reposición: cada cuánto llega el proveedor.
ALTER TABLE configuracion
    ADD COLUMN abc_dias_reposicion INT NOT NULL DEFAULT 7,
    ADD COLUMN abc_seguridad_a     INT NOT NULL DEFAULT 3,
    ADD COLUMN abc_seguridad_b     INT NOT NULL DEFAULT 2,
    ADD COLUMN abc_seguridad_c     INT NOT NULL DEFAULT 1,
    ADD COLUMN abc_compra_a        INT NOT NULL DEFAULT 7,
    ADD COLUMN abc_compra_b        INT NOT NULL DEFAULT 10,
    ADD COLUMN abc_compra_c        INT NOT NULL DEFAULT 15;
