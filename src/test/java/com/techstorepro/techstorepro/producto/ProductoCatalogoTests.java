package com.techstorepro.techstorepro.producto;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.math.BigDecimal;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class ProductoCatalogoTests {
    @Test
    void rangoInvertidoSeRechazaAntesDeConsultarProductos() {
        var repository = mock(ProductoRepository.class);
        var service = new ProductoService(repository);
        var filtro = new CatalogoFiltro(null, null, new BigDecimal("200"), new BigDecimal("100"), null, null, null, null, null);
        var error = assertThrows(ResponseStatusException.class, () -> service.catalogo(filtro));
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        verifyNoInteractions(repository);
    }

    @Test
    void contratoRechazaConsultasSinLimitesValidos() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            var filtro = new CatalogoFiltro(null, "x".repeat(101), new BigDecimal("-1"), null, null, null, "precio; drop table productos", -1, 1000);
            var fields = validator.validate(filtro).stream().map(v -> v.getPropertyPath().toString()).toList();
            assertTrue(fields.containsAll(java.util.List.of("q", "precioMin", "orden", "pagina", "tamanio")));
            assertTrue(validator.validate(new CatalogoFiltro(null, null, null, null, null, null, null, null, null)).isEmpty());
        }
    }
}
