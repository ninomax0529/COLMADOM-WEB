-- Segunda fase del ABC, paso 1: demanda diaria de cada artículo.
-- demanda_diaria = unidades base vendidas / dias_con_datos. Los días con datos
-- son los del período, pero si el artículo se creó después o el sistema empezó
-- a facturar después, se cuentan solo los días reales (si no, se subestima).
-- Los días de cobertura (existencia / demanda) se calculan al momento en la
-- pantalla, con la existencia actual.
ALTER TABLE articulo_clasificacion_abc
    ADD COLUMN dias_con_datos INT           NOT NULL DEFAULT 0,
    ADD COLUMN demanda_diaria DECIMAL(12,4) NOT NULL DEFAULT 0;
