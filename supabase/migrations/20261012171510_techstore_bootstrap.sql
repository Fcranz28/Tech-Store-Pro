-- Initial Supabase schema built from the existing Flyway V1-V8 migrations.
-- Supabase owns deployment migration history; Spring only validates this schema.
CREATE SCHEMA techstore;
REVOKE ALL ON SCHEMA techstore FROM PUBLIC;
SET LOCAL search_path TO techstore, public;
-- Source: V1__create_users.sql
CREATE TABLE usuarios (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    rol VARCHAR(20) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Source: V2__commerce.sql
CREATE TABLE productos (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(140) NOT NULL,
    descripcion TEXT NOT NULL,
    categoria VARCHAR(60) NOT NULL,
    precio NUMERIC(12,2) NOT NULL CHECK (precio >= 0),
    stock INTEGER NOT NULL CHECK (stock >= 0),
    imagen_url VARCHAR(500),
    destacado BOOLEAN NOT NULL DEFAULT FALSE,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_productos_activo_categoria ON productos(activo, categoria);

CREATE TABLE carrito_items (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    producto_id BIGINT NOT NULL REFERENCES productos(id),
    cantidad INTEGER NOT NULL CHECK (cantidad > 0),
    CONSTRAINT uq_carrito_usuario_producto UNIQUE (usuario_id, producto_id)
);
CREATE INDEX idx_carrito_usuario ON carrito_items(usuario_id);

CREATE TABLE pedidos (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id),
    total NUMERIC(12,2) NOT NULL CHECK (total >= 0),
    estado VARCHAR(30) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_pedidos_usuario_fecha ON pedidos(usuario_id, created_at DESC);

CREATE TABLE pedido_detalles (
    id BIGSERIAL PRIMARY KEY,
    pedido_id BIGINT NOT NULL REFERENCES pedidos(id),
    producto_id BIGINT NOT NULL REFERENCES productos(id),
    producto_nombre VARCHAR(140) NOT NULL,
    precio_unitario NUMERIC(12,2) NOT NULL CHECK (precio_unitario >= 0),
    cantidad INTEGER NOT NULL CHECK (cantidad > 0),
    subtotal NUMERIC(12,2) NOT NULL CHECK (subtotal >= 0)
);
CREATE INDEX idx_pedido_detalles_pedido ON pedido_detalles(pedido_id);

-- Source: V3__sample_products.sql
INSERT INTO productos (nombre, descripcion, categoria, precio, stock, imagen_url, destacado, activo) VALUES
('Teclado mecánico K67', 'Formato compacto, switches precisos e iluminación azul para una experiencia de escritura sólida.', 'TECLADOS', 279.00, 18, '/assets/catalog-contact-sheet.png', TRUE, TRUE),
('Mouse inalámbrico M8', 'Control cómodo y sensor preciso para trabajo y juego diario.', 'MOUSE', 149.00, 25, '/assets/catalog-contact-sheet.png', TRUE, TRUE),
('Audífonos inalámbricos Aural', 'Sonido envolvente y almohadillas cómodas para escuchar durante horas.', 'AUDIFONOS', 389.00, 12, '/assets/catalog-contact-sheet.png', TRUE, TRUE),
('Earbuds Pulse', 'Audio compacto con estuche de carga para llevarlo a todas partes.', 'AUDIO', 229.00, 20, '/assets/catalog-contact-sheet.png', FALSE, TRUE);

-- Source: V4__estados_pedido.sql
ALTER TABLE pedidos ADD CONSTRAINT ck_pedidos_estado
    CHECK (estado IN ('CONFIRMADO', 'EN_PREPARACION', 'ENVIADO', 'ENTREGADO'));

-- Source: V5__cloudinary_product_images.sql
UPDATE productos
SET imagen_url = CASE categoria
    WHEN 'TECLADOS' THEN 'https://res.cloudinary.com/dgj2ol5r1/image/upload/c_crop,w_627,h_627,x_0,y_0/v1791215142/techstore-pro/catalog-contact-sheet.png'
    WHEN 'MOUSE' THEN 'https://res.cloudinary.com/dgj2ol5r1/image/upload/c_crop,w_627,h_627,x_627,y_0/v1791215142/techstore-pro/catalog-contact-sheet.png'
    WHEN 'AUDIFONOS' THEN 'https://res.cloudinary.com/dgj2ol5r1/image/upload/c_crop,w_627,h_627,x_627,y_627/v1791215142/techstore-pro/catalog-contact-sheet.png'
    WHEN 'AUDIO' THEN 'https://res.cloudinary.com/dgj2ol5r1/image/upload/c_crop,w_627,h_627,x_0,y_627/v1791215142/techstore-pro/catalog-contact-sheet.png'
END
WHERE imagen_url = '/assets/catalog-contact-sheet.png'
  AND categoria IN ('TECLADOS', 'MOUSE', 'AUDIFONOS', 'AUDIO');

-- Source: V6__expand_sample_catalog.sql
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

-- Source: V7__devoluciones.sql
CREATE TABLE devoluciones (
    id BIGSERIAL PRIMARY KEY,
    pedido_id BIGINT NOT NULL REFERENCES pedidos(id),
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id),
    estado VARCHAR(20) NOT NULL DEFAULT 'SOLICITADA' CHECK (estado IN ('SOLICITADA','EN_REVISION','APROBADA','RECHAZADA','COMPLETADA')),
    motivo VARCHAR(40) NOT NULL,
    comentario VARCHAR(1000),
    observacion_operador VARCHAR(1000),
    importe NUMERIC(12,2) NOT NULL CHECK (importe >= 0),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_devoluciones_usuario_fecha ON devoluciones(usuario_id, created_at DESC);
CREATE INDEX idx_devoluciones_estado_fecha ON devoluciones(estado, created_at DESC);
CREATE INDEX idx_devoluciones_motivo_fecha ON devoluciones(motivo, created_at DESC);

CREATE TABLE devolucion_detalles (
    id BIGSERIAL PRIMARY KEY,
    devolucion_id BIGINT NOT NULL REFERENCES devoluciones(id) ON DELETE CASCADE,
    pedido_detalle_id BIGINT NOT NULL REFERENCES pedido_detalles(id),
    cantidad INTEGER NOT NULL CHECK (cantidad > 0),
    precio_unitario NUMERIC(12,2) NOT NULL CHECK (precio_unitario >= 0),
    subtotal NUMERIC(12,2) NOT NULL CHECK (subtotal >= 0),
    CONSTRAINT uq_devolucion_detalle UNIQUE (devolucion_id, pedido_detalle_id)
);
CREATE INDEX idx_devolucion_detalle_compra ON devolucion_detalles(pedido_detalle_id);

-- Source: V8__restrict_database_api_access.sql
-- Authorization uses the Spring JWT and ownership checks. Direct Data API access
-- must not expose password hashes, carts, orders, or returns. The table owner
-- used by the backend retains access; non-owner roles have no RLS policies.
ALTER TABLE usuarios ENABLE ROW LEVEL SECURITY;
ALTER TABLE productos ENABLE ROW LEVEL SECURITY;
ALTER TABLE carrito_items ENABLE ROW LEVEL SECURITY;
ALTER TABLE pedidos ENABLE ROW LEVEL SECURITY;
ALTER TABLE pedido_detalles ENABLE ROW LEVEL SECURITY;
ALTER TABLE devoluciones ENABLE ROW LEVEL SECURITY;
ALTER TABLE devolucion_detalles ENABLE ROW LEVEL SECURITY;

DO $$
BEGIN
    IF current_schema() = 'techstore' THEN
        REVOKE ALL ON SCHEMA techstore FROM PUBLIC;
        IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'anon') THEN
            REVOKE ALL ON SCHEMA techstore FROM anon;
            REVOKE ALL ON ALL TABLES IN SCHEMA techstore FROM anon;
            REVOKE ALL ON ALL SEQUENCES IN SCHEMA techstore FROM anon;
        END IF;
        IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'authenticated') THEN
            REVOKE ALL ON SCHEMA techstore FROM authenticated;
            REVOKE ALL ON ALL TABLES IN SCHEMA techstore FROM authenticated;
            REVOKE ALL ON ALL SEQUENCES IN SCHEMA techstore FROM authenticated;
        END IF;
    END IF;
END $$;
