CREATE INDEX idx_carrito_producto ON carrito_items(producto_id);
CREATE INDEX idx_devoluciones_pedido ON devoluciones(pedido_id);
CREATE INDEX idx_pedido_detalles_producto ON pedido_detalles(producto_id);
