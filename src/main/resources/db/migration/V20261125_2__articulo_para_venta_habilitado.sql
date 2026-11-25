-- El formulario de artículo no guardaba para_venta ni habilitado, así que
-- quedaron en 0 aunque los artículos se usan. Se marcan todos como de venta y
-- habilitados (desde ahora se editan en el formulario) y los nuevos nacen así.
UPDATE articulo SET para_venta = 1, habilitado = 1;

ALTER TABLE articulo
    MODIFY para_venta BIT(1) NOT NULL DEFAULT b'1',
    MODIFY habilitado BIT(1) NOT NULL DEFAULT b'1';
