-- Demo catalog additions. Product photos are representative category images.
WITH sample_products(nombre, descripcion, categoria, precio, stock, imagen_url, destacado) AS (
    VALUES
    ('Teclado compacto K75', 'Teclado compacto para escritorios con poco espacio. Imagen referencial.', 'TECLADOS', 249.00, 16, 'https://res.cloudinary.com/dgj2ol5r1/image/upload/c_crop,w_627,h_627,x_0,y_0/v1791215142/techstore-pro/catalog-contact-sheet.png', FALSE),
    ('Teclado mecánico K87', 'Distribución TKL para escritura y juego diario. Imagen referencial.', 'TECLADOS', 319.00, 11, 'https://res.cloudinary.com/dgj2ol5r1/image/upload/c_crop,w_627,h_627,x_0,y_0/v1791215142/techstore-pro/catalog-contact-sheet.png', TRUE),
    ('Teclado inalámbrico Slim K80', 'Perfil delgado para un escritorio despejado. Imagen referencial.', 'TECLADOS', 199.00, 22, 'https://res.cloudinary.com/dgj2ol5r1/image/upload/c_crop,w_627,h_627,x_0,y_0/v1791215142/techstore-pro/catalog-contact-sheet.png', FALSE),
    ('Mouse ergonómico M20', 'Forma cómoda para largas jornadas de trabajo. Imagen referencial.', 'MOUSE', 169.00, 24, 'https://res.cloudinary.com/dgj2ol5r1/image/upload/c_crop,w_627,h_627,x_627,y_0/v1791215142/techstore-pro/catalog-contact-sheet.png', FALSE),
    ('Mouse gaming M90', 'Control preciso para partidas y tareas creativas. Imagen referencial.', 'MOUSE', 219.00, 14, 'https://res.cloudinary.com/dgj2ol5r1/image/upload/c_crop,w_627,h_627,x_627,y_0/v1791215142/techstore-pro/catalog-contact-sheet.png', TRUE),
    ('Mouse compacto M15', 'Diseño ligero para llevar junto a tu portátil. Imagen referencial.', 'MOUSE', 99.00, 30, 'https://res.cloudinary.com/dgj2ol5r1/image/upload/c_crop,w_627,h_627,x_627,y_0/v1791215142/techstore-pro/catalog-contact-sheet.png', FALSE),
    ('Audífonos Studio A2', 'Escucha cómoda en casa o en la oficina. Imagen referencial.', 'AUDIFONOS', 429.00, 9, 'https://res.cloudinary.com/dgj2ol5r1/image/upload/c_crop,w_627,h_627,x_627,y_627/v1791215142/techstore-pro/catalog-contact-sheet.png', TRUE),
    ('Audífonos Aural Lite', 'Formato ligero para acompañar tus rutinas. Imagen referencial.', 'AUDIFONOS', 289.00, 17, 'https://res.cloudinary.com/dgj2ol5r1/image/upload/c_crop,w_627,h_627,x_627,y_627/v1791215142/techstore-pro/catalog-contact-sheet.png', FALSE),
    ('Audífonos Aural Pro', 'Una opción cómoda para música y llamadas. Imagen referencial.', 'AUDIFONOS', 479.00, 8, 'https://res.cloudinary.com/dgj2ol5r1/image/upload/c_crop,w_627,h_627,x_627,y_627/v1791215142/techstore-pro/catalog-contact-sheet.png', FALSE),
    ('Earbuds Pulse Mini', 'Audio de bolsillo para tus trayectos diarios. Imagen referencial.', 'AUDIO', 179.00, 26, 'https://res.cloudinary.com/dgj2ol5r1/image/upload/c_crop,w_627,h_627,x_0,y_627/v1791215142/techstore-pro/catalog-contact-sheet.png', FALSE),
    ('Earbuds Pulse Plus', 'Auriculares compactos para escuchar donde estés. Imagen referencial.', 'AUDIO', 259.00, 19, 'https://res.cloudinary.com/dgj2ol5r1/image/upload/c_crop,w_627,h_627,x_0,y_627/v1791215142/techstore-pro/catalog-contact-sheet.png', TRUE)
)
INSERT INTO productos (nombre, descripcion, categoria, precio, stock, imagen_url, destacado, activo)
SELECT s.nombre, s.descripcion, s.categoria, s.precio, s.stock, s.imagen_url, s.destacado, TRUE
FROM sample_products s
WHERE NOT EXISTS (SELECT 1 FROM productos p WHERE p.nombre = s.nombre);
