package com.techstorepro.techstorepro.carrito;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/carrito/invitado")
public class CarritoInvitadoController {
    private final CarritoInvitadoService service;
    public CarritoInvitadoController(CarritoInvitadoService service) { this.service = service; }

    public record TokenRequest(String token) {}
    public record ItemRequest(String token, @NotNull Long productoId, @Min(1) @Max(1000) int cantidad) {}
    public record QuantityRequest(String token, @Min(1) @Max(1000) int cantidad) {}

    @PostMapping("/actual")
    public CarritoInvitadoService.InvitadoResponse current(@RequestBody TokenRequest request) {
        return service.current(request.token());
    }
    @PostMapping("/items")
    public CarritoInvitadoService.InvitadoResponse add(@Valid @RequestBody ItemRequest request) {
        return service.add(request.token(), request.productoId(), request.cantidad());
    }
    @PutMapping("/items/{id}")
    public CarritoInvitadoService.InvitadoResponse update(@PathVariable Long id, @Valid @RequestBody QuantityRequest request) {
        return service.update(request.token(), id, request.cantidad());
    }
    @PostMapping("/items/{id}/eliminar")
    public CarritoInvitadoService.InvitadoResponse remove(@PathVariable Long id, @RequestBody TokenRequest request) {
        return service.remove(request.token(), id);
    }
    @PostMapping("/transferir")
    public CarritoResponse transfer(@AuthenticationPrincipal Jwt jwt, @RequestBody TokenRequest request) {
        return service.transfer(jwt.getSubject(), request.token());
    }
}
