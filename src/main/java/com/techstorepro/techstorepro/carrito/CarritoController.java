package com.techstorepro.techstorepro.carrito;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/carrito")
public class CarritoController {
    private final CarritoService carrito;
    public CarritoController(CarritoService carrito) { this.carrito = carrito; }
    public record ItemRequest(@NotNull Long productoId, @Min(1) @Max(1000) int cantidad) {}
    public record QuantityRequest(@Min(1) @Max(1000) int cantidad) {}
    @GetMapping
    public CarritoResponse get(@org.springframework.security.core.annotation.AuthenticationPrincipal Jwt jwt) {
        return carrito.get(jwt.getSubject());
    }
    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    public CarritoResponse add(@org.springframework.security.core.annotation.AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ItemRequest request) {
        return carrito.add(jwt.getSubject(), request.productoId(), request.cantidad());
    }
    @PutMapping("/items/{id}")
    public CarritoResponse update(@org.springframework.security.core.annotation.AuthenticationPrincipal Jwt jwt,
            @PathVariable Long id, @Valid @RequestBody QuantityRequest request) {
        return carrito.update(jwt.getSubject(), id, request.cantidad());
    }
    @DeleteMapping("/items/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@org.springframework.security.core.annotation.AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        carrito.remove(jwt.getSubject(), id);
    }
}
