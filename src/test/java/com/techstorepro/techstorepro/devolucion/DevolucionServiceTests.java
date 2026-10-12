package com.techstorepro.techstorepro.devolucion;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.techstorepro.techstorepro.devolucion.DevolucionDtos.*;
import com.techstorepro.techstorepro.pedido.*;
import com.techstorepro.techstorepro.usuario.*;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class DevolucionServiceTests {
    private final DevolucionRepository devoluciones = mock(DevolucionRepository.class);
    private final DevolucionDetalleRepository detalles = mock(DevolucionDetalleRepository.class);
    private final PedidoRepository pedidos = mock(PedidoRepository.class);
    private final PedidoDetalleRepository lineas = mock(PedidoDetalleRepository.class);
    private final UsuarioRepository usuarios = mock(UsuarioRepository.class);
    private DevolucionService service;
    private Pedido pedido;
    private PedidoDetalle linea;

    @BeforeEach void setUp() throws Exception {
        service = new DevolucionService(devoluciones, detalles, pedidos, lineas, usuarios);
        Usuario user = mock(Usuario.class);
        when(user.getId()).thenReturn(5L);
        when(usuarios.findByEmail("buyer@example.com")).thenReturn(Optional.of(user));
        pedido = new Pedido(5L, new BigDecimal("120.00"));
        setId(pedido, 7L); pedido.avanzarEstado(EstadoPedido.ENTREGADO);
        linea = new PedidoDetalle(7L, 10L, "Mouse", new BigDecimal("40.00"), 3);
        setId(linea, 11L);
        when(pedidos.lockById(7L)).thenReturn(Optional.of(pedido));
        when(lineas.findByPedidoId(7L)).thenReturn(List.of(linea));
        when(detalles.cantidadConsumida(eq(11L), any())).thenReturn(0L);
    }
    private static void setId(Object target, Long value) throws Exception {
        Field field = target.getClass().getDeclaredField("id"); field.setAccessible(true); field.set(target, value);
    }
    private Solicitud request(int cantidad) {
        return new Solicitud(7L, "PRODUCTO_DEFECTUOSO", "No funciona", List.of(new LineaSolicitud(11L, cantidad)));
    }
    @Test void validReturnUsesOriginalPurchasePrice() throws Exception {
        when(devoluciones.saveAndFlush(any())).thenAnswer(call -> {
            Devolucion d = call.getArgument(0); setId(d, 21L); return d;
        });
        when(detalles.findByDevolucionIdIn(List.of(21L))).thenReturn(List.of(new DevolucionDetalle(21L, 11L, 2, new BigDecimal("40.00"))));
        Respuesta result = service.solicitar("buyer@example.com", request(2));
        assertEquals(new BigDecimal("80.00"), result.importe());
        assertEquals(EstadoDevolucion.SOLICITADA, result.estado());
        verify(detalles).saveAll(any());
    }
    @Test void excessiveOrAlreadyReturnedQuantityIsRejected() {
        when(detalles.cantidadConsumida(eq(11L), any())).thenReturn(1L);
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
            () -> service.solicitar("buyer@example.com", request(3)));
        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        verify(devoluciones, never()).saveAndFlush(any());
    }
    @Test void anotherCustomersPurchaseIsHidden() {
        Pedido foreign = new Pedido(99L, BigDecimal.TEN);
        when(pedidos.lockById(7L)).thenReturn(Optional.of(foreign));
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
            () -> service.solicitar("buyer@example.com", request(1)));
        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
    }
    @Test void onlyAllowedTransitionsAndReasonedRejectionsProceed() throws Exception {
        Devolucion d = new Devolucion(7L, 5L, "OTRO", null, BigDecimal.TEN);
        setId(d, 21L);
        when(devoluciones.lockById(21L)).thenReturn(Optional.of(d));
        assertEquals(EstadoDevolucion.EN_REVISION,
            service.cambiar(21L, new CambioEstado(EstadoDevolucion.SOLICITADA, EstadoDevolucion.EN_REVISION, null)).estado());
        assertEquals(HttpStatus.BAD_REQUEST, assertThrows(ResponseStatusException.class,
            () -> service.cambiar(21L, new CambioEstado(EstadoDevolucion.EN_REVISION, EstadoDevolucion.RECHAZADA, " "))).getStatusCode());
        assertEquals(HttpStatus.CONFLICT, assertThrows(ResponseStatusException.class,
            () -> service.cambiar(21L, new CambioEstado(EstadoDevolucion.EN_REVISION, EstadoDevolucion.COMPLETADA, null))).getStatusCode());
        assertEquals(EstadoDevolucion.RECHAZADA,
            service.cambiar(21L, new CambioEstado(EstadoDevolucion.EN_REVISION, EstadoDevolucion.RECHAZADA, "Sin defecto")).estado());
    }
}
