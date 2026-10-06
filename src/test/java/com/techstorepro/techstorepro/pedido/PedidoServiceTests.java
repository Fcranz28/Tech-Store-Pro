package com.techstorepro.techstorepro.pedido;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.techstorepro.techstorepro.carrito.CarritoItem;
import com.techstorepro.techstorepro.carrito.CarritoItemRepository;
import com.techstorepro.techstorepro.producto.Producto;
import com.techstorepro.techstorepro.producto.ProductoRepository;
import com.techstorepro.techstorepro.producto.ProductoRequest;
import com.techstorepro.techstorepro.usuario.Usuario;
import com.techstorepro.techstorepro.usuario.UsuarioRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class PedidoServiceTests {
    private final PedidoRepository pedidos = mock(PedidoRepository.class);
    private final PedidoDetalleRepository detalles = mock(PedidoDetalleRepository.class);
    private final CarritoItemRepository carrito = mock(CarritoItemRepository.class);
    private final ProductoRepository productos = mock(ProductoRepository.class);
    private final UsuarioRepository usuarios = mock(UsuarioRepository.class);
    private PedidoService service;

    @BeforeEach
    void setUp() {
        service = new PedidoService(pedidos, detalles, carrito, productos, usuarios);
        Usuario user = mock(Usuario.class);
        when(user.getId()).thenReturn(5L);
        when(usuarios.lockByEmail("buyer@example.com")).thenReturn(Optional.of(user));
    }

    @Test
    void emptyCartCannotBeConfirmed() {
        when(carrito.findByUsuarioIdOrderById(5L)).thenReturn(List.of());
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
            () -> service.confirm("buyer@example.com"));
        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        verify(pedidos, never()).saveAndFlush(any());
    }

    @Test
    void orderAdvancesThroughEveryStateAndCannotAdvanceAfterDelivery() {
        Pedido pedido = new Pedido(5L, new BigDecimal("49.00"));
        when(pedidos.lockById(7L)).thenReturn(Optional.of(pedido));
        when(detalles.findByPedidoIdIn(List.of(7L))).thenReturn(List.of());
        EstadoPedido actual = EstadoPedido.CONFIRMADO;
        for (EstadoPedido next : List.of(EstadoPedido.EN_PREPARACION, EstadoPedido.ENVIADO, EstadoPedido.ENTREGADO)) {
            assertEquals(next.name(), service.actualizarEstado(7L, new ActualizarEstadoRequest(actual, next)).estado());
            actual = next;
        }
        assertThrows(ResponseStatusException.class, () -> service.actualizarEstado(7L,
            new ActualizarEstadoRequest(EstadoPedido.ENTREGADO, EstadoPedido.CONFIRMADO)));
        assertEquals("ENTREGADO", pedido.getEstado());
        verifyNoInteractions(productos, carrito);
    }

    @Test
    void skippedAndStaleTransitionsDoNotChangeOrder() {
        Pedido pedido = new Pedido(5L, new BigDecimal("49.00"));
        when(pedidos.lockById(7L)).thenReturn(Optional.of(pedido));
        for (ActualizarEstadoRequest request : List.of(
                new ActualizarEstadoRequest(EstadoPedido.CONFIRMADO, EstadoPedido.ENVIADO),
                new ActualizarEstadoRequest(EstadoPedido.EN_PREPARACION, EstadoPedido.ENVIADO))) {
            ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.actualizarEstado(7L, request));
            assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
            assertEquals("CONFIRMADO", pedido.getEstado());
        }
    }

    @Test
    void missingOrderReturnsNotFound() {
        when(pedidos.lockById(7L)).thenReturn(Optional.empty());
        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> service.actualizarEstado(7L,
            new ActualizarEstadoRequest(EstadoPedido.CONFIRMADO, EstadoPedido.EN_PREPARACION)));
        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
    }

    @Test
    void insufficientStockDoesNotCreateOrderOrClearCart() {
        when(carrito.findByUsuarioIdOrderById(5L)).thenReturn(List.of(new CarritoItem(5L, 10L, 2)));
        Producto product = new Producto(new ProductoRequest("Mouse", "Descripción", "MOUSE",
            new BigDecimal("49.00"), 1, null, false, true));
        when(productos.lockById(10L)).thenReturn(Optional.of(product));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
            () -> service.confirm("buyer@example.com"));

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        assertEquals(1, product.getStock());
        verify(pedidos, never()).saveAndFlush(any());
        verify(carrito, never()).deleteByUsuarioId(any());
    }
}
