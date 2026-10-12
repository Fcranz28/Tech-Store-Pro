package com.techstorepro.techstorepro.devolucion;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "devolucion_detalles")
public class DevolucionDetalle {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "devolucion_id", nullable = false) private Long devolucionId;
    @Column(name = "pedido_detalle_id", nullable = false) private Long pedidoDetalleId;
    @Column(nullable = false) private int cantidad;
    @Column(name = "precio_unitario", nullable = false, precision = 12, scale = 2) private BigDecimal precioUnitario;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal subtotal;
    protected DevolucionDetalle() {}
    public DevolucionDetalle(Long devolucionId, Long pedidoDetalleId, int cantidad, BigDecimal precioUnitario) {
        this.devolucionId = devolucionId; this.pedidoDetalleId = pedidoDetalleId;
        this.cantidad = cantidad; this.precioUnitario = precioUnitario;
        this.subtotal = precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }
    public Long getDevolucionId() { return devolucionId; }
    public Long getPedidoDetalleId() { return pedidoDetalleId; }
    public int getCantidad() { return cantidad; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public BigDecimal getSubtotal() { return subtotal; }
}
