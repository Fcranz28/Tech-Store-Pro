package com.techstorepro.techstorepro.producto;

import java.math.BigDecimal;

public record ProductoResponse(Long id, String nombre, String descripcion, String categoria,
        BigDecimal precio, int stock, String imagenUrl, boolean destacado, boolean activo) {
    public static ProductoResponse from(Producto p) {
        return new ProductoResponse(p.getId(), p.getNombre(), p.getDescripcion(), p.getCategoria(),
            p.getPrecio(), p.getStock(), p.getImagenUrl(), p.isDestacado(), p.isActivo());
    }
}
