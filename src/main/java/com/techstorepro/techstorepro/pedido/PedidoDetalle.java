package com.techstorepro.techstorepro.pedido;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "pedido_detalles")
public class PedidoDetalle {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "pedido_id", nullable = false) private Long pedidoId;
    @Column(name = "producto_id", nullable = false) private Long productoId;
    @Column(name = "producto_nombre", nullable = false, length = 140) private String productoNombre;
    @Column(name = "precio_unitario", nullable = false, precision = 12, scale = 2) private BigDecimal precioUnitario;
    @Column(nullable = false) private int cantidad;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal subtotal;
    protected PedidoDetalle() {}
    public PedidoDetalle(Long pedidoId, Long productoId, String productoNombre, BigDecimal precioUnitario, int cantidad) {
        this.pedidoId = pedidoId; this.productoId = productoId; this.productoNombre = productoNombre;
        this.precioUnitario = precioUnitario; this.cantidad = cantidad;
        this.subtotal = precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }
    public Long getProductoId() { return productoId; }
    public String getProductoNombre() { return productoNombre; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public int getCantidad() { return cantidad; }
    public BigDecimal getSubtotal() { return subtotal; }
    public Long getPedidoId() { return pedidoId; }
}
