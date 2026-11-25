-- Segunda fase del ABC, paso 4: conteo cíclico.
-- Cada día se cuentan unos pocos artículos de un almacén (los que tocan según
-- su clase ABC). Las diferencias se registran como ajuste de inventario
-- (INCREMENTO y/o DISMINUCION), así quedan en el kardex.
CREATE TABLE conteo_inventario (
    codigo             INT AUTO_INCREMENT PRIMARY KEY,
    fecha              DATE         NOT NULL,
    fecha_registro     DATETIME     NOT NULL,
    almacen            INT          NOT NULL,
    nombre_almacen     VARCHAR(50)  NULL,
    nombre_usuario     VARCHAR(80)  NULL,
    observacion        VARCHAR(255) NULL,
    articulos          INT          NOT NULL DEFAULT 0,
    con_diferencia     INT          NOT NULL DEFAULT 0,
    ajuste_incremento  INT          NULL,
    ajuste_disminucion INT          NULL,
    CONSTRAINT fk_conteo_almacen FOREIGN KEY (almacen) REFERENCES almacen (codigo),
    CONSTRAINT fk_conteo_ajuste_inc FOREIGN KEY (ajuste_incremento) REFERENCES ajuste_inventario (codigo),
    CONSTRAINT fk_conteo_ajuste_dis FOREIGN KEY (ajuste_disminucion) REFERENCES ajuste_inventario (codigo),
    INDEX idx_conteo_almacen_fecha (almacen, fecha)
);

-- Cantidades en la unidad base del artículo.
CREATE TABLE detalle_conteo_inventario (
    codigo               INT AUTO_INCREMENT PRIMARY KEY,
    conteo               INT           NOT NULL,
    articulo             INT           NOT NULL,
    descripcion_articulo VARCHAR(120)  NULL,
    clase                CHAR(1)       NULL,
    existencia_sistema   DECIMAL(12,2) NOT NULL,
    cantidad_contada     DECIMAL(12,2) NOT NULL,
    diferencia           DECIMAL(12,2) NOT NULL,
    CONSTRAINT fk_det_conteo FOREIGN KEY (conteo) REFERENCES conteo_inventario (codigo),
    CONSTRAINT fk_det_conteo_articulo FOREIGN KEY (articulo) REFERENCES articulo (codigo),
    INDEX idx_det_conteo_articulo (articulo)
);

-- Cada cuántos días se cuenta cada clase y cuántos artículos por día.
ALTER TABLE configuracion
    ADD COLUMN abc_conteo_dias_a  INT NOT NULL DEFAULT 7,
    ADD COLUMN abc_conteo_dias_b  INT NOT NULL DEFAULT 30,
    ADD COLUMN abc_conteo_dias_c  INT NOT NULL DEFAULT 90,
    ADD COLUMN abc_conteo_por_dia INT NOT NULL DEFAULT 10;
