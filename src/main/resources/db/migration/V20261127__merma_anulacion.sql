-- Anulación de mermas: la merma queda marcada como anulada (no se borra)
-- y una entrada de inventario devuelve la existencia a los mismos lotes.
ALTER TABLE merma
    ADD COLUMN anulada BIT(1) NOT NULL DEFAULT b'0',
    ADD COLUMN fecha_anulacion DATETIME NULL,
    ADD COLUMN motivo_anulacion VARCHAR(255) NULL,
    ADD COLUMN usuario_anulacion VARCHAR(100) NULL,
    ADD COLUMN entrada_anulacion INT NULL,
    ADD CONSTRAINT fk_merma_entrada_anulacion FOREIGN KEY (entrada_anulacion) REFERENCES entrada_inventario (codigo);
