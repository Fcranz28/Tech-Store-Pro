package com.techstorepro.techstorepro.admin;

import com.techstorepro.techstorepro.pedido.EstadoPedido;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminDashboardService {
    private final AdminDashboardRepository repository;
    public AdminDashboardService(AdminDashboardRepository repository) { this.repository = repository; }

    // All cards use the same database snapshot, even when purchases arrive during a refresh.
    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public AdminDashboardResponse resumen() {
        var pedidos = repository.totalesPedidos();
        var productos = repository.totalesProductos();
        var porEstado = repository.pedidosPorEstado();
        var estados = Arrays.stream(EstadoPedido.values()).map(estado -> {
            long cantidad = porEstado.getOrDefault(estado.name(), 0L);
            BigDecimal porcentaje = pedidos.pedidos() == 0 ? BigDecimal.ZERO : BigDecimal.valueOf(cantidad)
                .multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(pedidos.pedidos()), 2, RoundingMode.HALF_UP);
            return new AdminDashboardResponse.ResumenEstado(estado.name(), switch (estado) {
                case CONFIRMADO -> "Confirmado";
                case EN_PREPARACION -> "En preparación";
                case ENVIADO -> "Enviado";
                case ENTREGADO -> "Entregado";
            }, cantidad, porcentaje);
        }).toList();
        return new AdminDashboardResponse(pedidos.importe(), pedidos.clientes(), pedidos.pedidos(), pedidos.pendientes(),
            productos.total(), productos.visibles(), productos.conStock(), productos.agotados(),
            repository.pedidosRecientes(), estados, repository.productosMasPedidos());
    }
}
