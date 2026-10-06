package com.techstorepro.techstorepro.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.techstorepro.techstorepro.carrito.CarritoInvitadoService;
import com.techstorepro.techstorepro.carrito.CarritoResponse;
import com.techstorepro.techstorepro.carrito.CarritoService;
import com.techstorepro.techstorepro.producto.Producto;
import com.techstorepro.techstorepro.producto.ProductoRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class CarritoInvitadoServiceTests {
    private final ProductoRepository products = mock(ProductoRepository.class);
    private final CarritoService userCart = mock(CarritoService.class);
    private final SecurityConfig security = new SecurityConfig();
    private final JwtTools jwt = new JwtTools();
    private final CarritoInvitadoService guests = new CarritoInvitadoService(products, userCart,
        jwt.encoder, jwt.decoder, "techstore-pro");

    @Test
    void signedGuestCartSurvivesReloadAndTransfersToUser() {
        Producto product = mock(Producto.class);
        when(product.getId()).thenReturn(7L);
        when(product.getNombre()).thenReturn("Mouse");
        when(product.getCategoria()).thenReturn("MOUSE");
        when(product.getPrecio()).thenReturn(new BigDecimal("50.00"));
        when(product.getStock()).thenReturn(5);
        when(products.findByIdAndActivoTrue(7L)).thenReturn(Optional.of(product));
        when(userCart.get("ana@example.com")).thenReturn(new CarritoResponse(List.of(), BigDecimal.ZERO));

        var added = guests.add(null, 7L, 2);
        var restored = guests.current(added.token());
        assertEquals(2, restored.carrito().items().get(0).cantidad());
        assertEquals(new BigDecimal("100.00"), restored.carrito().total());

        guests.transfer("ana@example.com", restored.token());
        verify(userCart).add("ana@example.com", 7L, 2);
    }

    @Test
    void rejectsTamperedGuestToken() {
        var error = assertThrows(ResponseStatusException.class, () -> guests.current("not-a-jwt"));
        assertEquals(HttpStatus.UNAUTHORIZED, error.getStatusCode());
    }

    private final class JwtTools {
        private final org.springframework.security.oauth2.jwt.JwtEncoder encoder;
        private final org.springframework.security.oauth2.jwt.JwtDecoder decoder;
        private JwtTools() {
            var key = security.jwtSecretKey("a-test-secret-that-is-longer-than-thirty-two-bytes");
            encoder = security.jwtEncoder(key);
            decoder = security.jwtDecoder(key, "techstore-pro");
        }
    }
}
