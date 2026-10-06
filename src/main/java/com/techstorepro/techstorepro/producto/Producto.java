package com.techstorepro.techstorepro.producto;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "productos")
public class Producto {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 140) private String nombre;
    @Column(nullable = false, columnDefinition = "text") private String descripcion;
    @Column(nullable = false, length = 60) private String categoria;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal precio;
    @Column(nullable = false) private int stock;
    @Column(name = "imagen_url", length = 500) private String imagenUrl;
    @Column(nullable = false) private boolean destacado;
    @Column(nullable = false) private boolean activo = true;

    protected Producto() {}
    public Producto(ProductoRequest request) { update(request); }
    public void update(ProductoRequest request) {
        nombre = request.nombre().trim();
        descripcion = request.descripcion().trim();
        categoria = request.categoria().trim().toUpperCase();
        precio = request.precio();
        stock = request.stock();
        imagenUrl = request.imagenUrl();
        destacado = request.destacado();
        activo = request.activo();
    }
    public void decreaseStock(int cantidad) { stock -= cantidad; }
    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public String getCategoria() { return categoria; }
    public BigDecimal getPrecio() { return precio; }
    public int getStock() { return stock; }
    public String getImagenUrl() { return imagenUrl; }
    public boolean isDestacado() { return destacado; }
    public boolean isActivo() { return activo; }
    public void deactivate() { activo = false; }
}
