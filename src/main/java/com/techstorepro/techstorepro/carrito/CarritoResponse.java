package com.techstorepro.techstorepro.carrito;

import java.math.BigDecimal;
import java.util.List;

public record CarritoResponse(List<Item> items, BigDecimal total) {
    public record Item(Long id, Long productoId, String nombre, String categoria, String imagenUrl,
            BigDecimal precio, int stock, int cantidad, BigDecimal subtotal) {}
}
