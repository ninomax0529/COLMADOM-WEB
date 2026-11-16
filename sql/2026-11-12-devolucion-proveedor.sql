-- ============================================================
-- Devolución de mercancía a proveedor (por lote)
-- Liquidación: NOTA_CREDITO (rebaja CxP), CAMBIO (reposición de
-- mercancía en un lote nuevo) o EFECTIVO (reembolso a la caja).
-- 2026-11-12
-- ============================================================

INSERT INTO tipo_documento (codigo, nombre, modulo, fecha_creacion, creado_por)
SELECT 10, 'DEVOLUCION A PROVEEDOR', 1, NOW(), 'ADMIN'
WHERE NOT EXISTS (SELECT 1 FROM tipo_documento WHERE codigo = 10);

CREATE TABLE devolucion_proveedor (
    codigo              INT NOT NULL AUTO_INCREMENT,
    fecha               DATETIME NOT NULL,
    proveedor           INT NOT NULL,
    almacen             INT NOT NULL,
    tipo_liquidacion    VARCHAR(20) NOT NULL,          -- NOTA_CREDITO | CAMBIO | EFECTIVO
    motivo              VARCHAR(20) NOT NULL,          -- VENCIDO | DANADO | OTRO
    observacion         VARCHAR(250) NULL,
    factura_compra      INT NULL,                      -- factura de referencia / a la que se aplicó la nota
    sub_total           DECIMAL(14,2) NOT NULL DEFAULT 0,
    itbis               DECIMAL(14,2) NOT NULL DEFAULT 0,
    total               DECIMAL(14,2) NOT NULL DEFAULT 0,
    credito_aplicado    DECIMAL(14,2) NOT NULL DEFAULT 0,
    credito_disponible  DECIMAL(14,2) NOT NULL DEFAULT 0,
    caja_turno          INT NULL,                      -- turno donde entró el reembolso en efectivo
    salida_inventario   INT NULL,
    entrada_inventario  INT NULL,                      -- reposición (CAMBIO)
    anulada             BIT(1) NOT NULL DEFAULT b'0',
    fecha_anulada       DATETIME NULL,
    motivo_anulacion    VARCHAR(250) NULL,
    usuario             VARCHAR(60) NULL,
    fecha_creacion      DATETIME NOT NULL,
    PRIMARY KEY (codigo),
    KEY idx_devprov_proveedor (proveedor),
    CONSTRAINT fk_devprov_proveedor FOREIGN KEY (proveedor) REFERENCES proveedor (codigo),
    CONSTRAINT fk_devprov_almacen FOREIGN KEY (almacen) REFERENCES almacen (codigo),
    CONSTRAINT fk_devprov_factura FOREIGN KEY (factura_compra) REFERENCES factura_de_compra (codigo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE detalle_devolucion_proveedor (
    codigo                   INT NOT NULL AUTO_INCREMENT,
    devolucion               INT NOT NULL,
    articulo                 INT NOT NULL,
    articulo_empaque         INT NOT NULL,
    descripcion_articulo     VARCHAR(250) NULL,
    cantidad                 DECIMAL(12,4) NOT NULL,   -- en el empaque elegido
    factor_conversion        DECIMAL(12,4) NOT NULL DEFAULT 1,
    cantidad_base            DECIMAL(12,4) NOT NULL,   -- cantidad x factor (unidad base)
    costo_unitario           DECIMAL(12,4) NOT NULL DEFAULT 0, -- por empaque, sin ITBIS
    porcentaje_itbis         DECIMAL(5,2) NOT NULL DEFAULT 0,
    sub_total                DECIMAL(14,2) NOT NULL DEFAULT 0,
    itbis                    DECIMAL(14,2) NOT NULL DEFAULT 0,
    total                    DECIMAL(14,2) NOT NULL DEFAULT 0,
    lote                     INT NULL,                 -- lote del que sale
    numero_lote_nuevo        VARCHAR(50) NULL,         -- CAMBIO: lote de la reposición
    fecha_vencimiento_nueva  DATE NULL,
    lote_nuevo               INT NULL,
    PRIMARY KEY (codigo),
    KEY idx_detdevprov_devolucion (devolucion),
    CONSTRAINT fk_detdevprov_devolucion FOREIGN KEY (devolucion) REFERENCES devolucion_proveedor (codigo),
    CONSTRAINT fk_detdevprov_articulo FOREIGN KEY (articulo) REFERENCES articulo (codigo),
    CONSTRAINT fk_detdevprov_empaque FOREIGN KEY (articulo_empaque) REFERENCES articulo_empaque (codigo),
    CONSTRAINT fk_detdevprov_lote FOREIGN KEY (lote) REFERENCES lote (codigo),
    CONSTRAINT fk_detdevprov_lote_nuevo FOREIGN KEY (lote_nuevo) REFERENCES lote (codigo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Cada aplicación de una nota de crédito a una factura de compra se registra
-- como un recibo de egreso sin efectivo (así el pendiente de CxP lo calcula
-- PAGO_FACT_COMPRA como siempre).
CREATE TABLE aplicacion_credito_proveedor (
    codigo          INT NOT NULL AUTO_INCREMENT,
    devolucion      INT NOT NULL,
    factura_compra  INT NOT NULL,
    recibo_egreso   INT NOT NULL,
    monto           DECIMAL(14,2) NOT NULL,
    fecha           DATETIME NOT NULL,
    usuario         VARCHAR(60) NULL,
    anulada         BIT(1) NOT NULL DEFAULT b'0',
    PRIMARY KEY (codigo),
    KEY idx_aplcred_devolucion (devolucion),
    CONSTRAINT fk_aplcred_devolucion FOREIGN KEY (devolucion) REFERENCES devolucion_proveedor (codigo),
    CONSTRAINT fk_aplcred_factura FOREIGN KEY (factura_compra) REFERENCES factura_de_compra (codigo),
    CONSTRAINT fk_aplcred_recibo FOREIGN KEY (recibo_egreso) REFERENCES recibo_de_egreso (codigo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
