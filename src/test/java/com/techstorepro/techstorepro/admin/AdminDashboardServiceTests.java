package com.techstorepro.techstorepro.admin;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AdminDashboardServiceTests {
    private final AdminDashboardRepository repository = mock(AdminDashboardRepository.class);
    private final AdminDashboardService service = new AdminDashboardService(repository);

    private void catalogoVacio() {
        when(repository.totalesProductos()).thenReturn(new AdminDashboardRepository.TotalesProductos(0, 0, 0, 0));
        when(repository.pedidosRecientes()).thenReturn(List.of());
        when(repository.productosMasPedidos()).thenReturn(List.of());
    }

    @Test
    void tiendaSinVentasNoDividePorCeroNiInventaActividad() {
        catalogoVacio();
        when(repository.totalesPedidos()).thenReturn(new AdminDashboardRepository.TotalesPedidos(BigDecimal.ZERO, 0, 0, 0));
        when(repository.pedidosPorEstado()).thenReturn(Map.of());
        var result = service.resumen();
        assertEquals(BigDecimal.ZERO, result.importePedidos());
        assertEquals(0, result.clientesConPedidos());
        assertTrue(result.pedidosRecientes().isEmpty());
        assertTrue(result.productosMasPedidos().isEmpty());
        assertEquals(4, result.estados().size());
        assertTrue(result.estados().stream().allMatch(estado -> estado.count() == 0 && estado.percent().signum() == 0));
    }

    @Test
    void conservaPrecisionMonetariaYCubreEstadosSinPedidos() {
        catalogoVacio();
        var importe = new BigDecimal("900719925474.13");
        when(repository.totalesPedidos()).thenReturn(new AdminDashboardRepository.TotalesPedidos(importe, 2, 3, 1));
        when(repository.pedidosPorEstado()).thenReturn(Map.of("CONFIRMADO", 1L, "ENTREGADO", 2L));
        var result = service.resumen();
        assertEquals(importe, result.importePedidos());
        assertEquals(2, result.clientesConPedidos());
        assertEquals(1, result.pedidosPendientes());
        assertEquals(new BigDecimal("33.33"), result.estados().get(0).percent());
        assertEquals(new BigDecimal("0.00"), result.estados().get(1).percent());
        assertEquals(new BigDecimal("66.67"), result.estados().get(3).percent());
    }
}
