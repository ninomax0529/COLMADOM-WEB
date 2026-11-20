-- Categorías del catálogo del POS (botones para filtrar las tarjetas).
-- Solo se insertan las que no existan, así que se puede correr sobre una base
-- que ya tenga categorías. Se pueden agregar más desde el formulario del
-- artículo escribiendo un nombre nuevo en "Categoría".

INSERT INTO categoria (nombre, creada_por, habilitada)
SELECT n.nombre, 'sistema', b'1'
FROM (
    SELECT 'Víveres' AS nombre, 1 AS orden
    UNION ALL SELECT 'Bebidas', 2
    UNION ALL SELECT 'Cervezas y Licores', 3
    UNION ALL SELECT 'Lácteos y Huevos', 4
    UNION ALL SELECT 'Embutidos y Carnes', 5
    UNION ALL SELECT 'Frutas y Vegetales', 6
    UNION ALL SELECT 'Panadería', 7
    UNION ALL SELECT 'Picaderas y Dulces', 8
    UNION ALL SELECT 'Limpieza', 9
    UNION ALL SELECT 'Higiene Personal', 10
    UNION ALL SELECT 'Cigarrillos', 11
    UNION ALL SELECT 'Otros', 12
) n
WHERE NOT EXISTS (SELECT 1 FROM categoria c WHERE c.nombre = n.nombre)
ORDER BY n.orden;
