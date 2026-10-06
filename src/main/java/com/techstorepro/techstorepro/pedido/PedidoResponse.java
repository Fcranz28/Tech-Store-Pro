package com.techstorepro.techstorepro.pedido;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record PedidoResponse(Long id, Long usuarioId, String estado, BigDecimal total, Instant createdAt,
        List<Linea> items) {
    public record Linea(Long productoId, String nombre, BigDecimal precioUnitario, int cantidad, BigDecimal subtotal) {
        public static Linea from(PedidoDetalle d) {
            return new Linea(d.getProductoId(), d.getProductoNombre(), d.getPrecioUnitario(), d.getCantidad(), d.getSubtotal());
        }
    }
}
