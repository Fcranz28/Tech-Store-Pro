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
