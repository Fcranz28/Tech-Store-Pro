package com.techstorepro.techstorepro.carrito;

import jakarta.persistence.*;

@Entity
@Table(name = "carrito_items")
public class CarritoItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "usuario_id", nullable = false) private Long usuarioId;
    @Column(name = "producto_id", nullable = false) private Long productoId;
    @Column(nullable = false) private int cantidad;
    protected CarritoItem() {}
    public CarritoItem(Long usuarioId, Long productoId, int cantidad) {
        this.usuarioId = usuarioId; this.productoId = productoId; this.cantidad = cantidad;
    }
    public Long getId() { return id; }
    public Long getUsuarioId() { return usuarioId; }
    public Long getProductoId() { return productoId; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
}
