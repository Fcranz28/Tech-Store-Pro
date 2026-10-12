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
