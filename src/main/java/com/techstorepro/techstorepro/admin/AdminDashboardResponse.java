package com.techstorepro.techstorepro.admin;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record AdminDashboardResponse(
    BigDecimal importePedidos, long clientesConPedidos, long totalPedidos, long pedidosPendientes,
    long totalProductos, long productosVisibles, long productosConStock, long productosAgotados,
    List<PedidoReciente> pedidosRecientes, List<ResumenEstado> estados, List<ProductoPopular> productosMasPedidos
) {
    public record PedidoReciente(long id, long usuarioId, String estado, BigDecimal total, Instant createdAt) {}
    public record ResumenEstado(String value, String label, long count, BigDecimal percent) {}
    public record ProductoPopular(long id, String nombre, long unidades, BigDecimal importe) {}
}
