-- Clasificación ABC automática de los artículos (por valor y por rotación).
-- La calcula ClasificacionAbcService cada noche y al arrancar si está vieja;
-- nadie la escribe a mano. Una fila por artículo con el último cálculo.
CREATE TABLE articulo_clasificacion_abc (
    articulo            INT           NOT NULL,
    -- A / B / C (combinada), o N = nuevo (creado hace poco, sin clasificar)
    clase               CHAR(1)       NOT NULL,
    clase_valor         CHAR(1)       NULL,
    clase_rotacion      CHAR(1)       NULL,
    -- Sin ventas en el período: mercancía parada (queda en C)
    sin_movimiento      BIT(1)        NOT NULL DEFAULT b'0',
    -- Ventas netas del período, sin ITBIS, sin anuladas y menos devoluciones
    ventas              DECIMAL(14,2) NOT NULL DEFAULT 0,
    -- En cuántas facturas apareció (rotación) y unidades base vendidas
    facturas            INT           NOT NULL DEFAULT 0,
    unidades            DECIMAL(14,2) NOT NULL DEFAULT 0,
    -- Participación del artículo en las ventas y en las facturas del período (%)
    porciento_valor     DECIMAL(7,4)  NOT NULL DEFAULT 0,
    porciento_rotacion  DECIMAL(7,4)  NOT NULL DEFAULT 0,
    fecha_desde         DATE          NOT NULL,
    fecha_hasta         DATE          NOT NULL,
    fecha_calculo       DATETIME      NOT NULL,
    PRIMARY KEY (articulo),
    KEY idx_abc_clase (clase),
    CONSTRAINT fk_abc_articulo FOREIGN KEY (articulo) REFERENCES articulo (codigo)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB;

-- Parámetros del cálculo (con valores por defecto; no hace falta tocarlos)
ALTER TABLE configuracion
    ADD COLUMN abc_dias_periodo     INT          NOT NULL DEFAULT 90,
    ADD COLUMN abc_limite_a         DECIMAL(5,2) NOT NULL DEFAULT 80.00,
    ADD COLUMN abc_limite_b         DECIMAL(5,2) NOT NULL DEFAULT 95.00,
    ADD COLUMN abc_dias_nuevo       INT          NOT NULL DEFAULT 30,
    ADD COLUMN abc_ultimo_calculo   DATETIME     NULL;
