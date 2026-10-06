package com.techstorepro.techstorepro.pedido;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {
    private final PedidoService pedidos;
    public PedidoController(PedidoService pedidos) { this.pedidos = pedidos; }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PedidoResponse confirm(@org.springframework.security.core.annotation.AuthenticationPrincipal Jwt jwt) {
        return pedidos.confirm(jwt.getSubject());
    }
    @GetMapping
    public List<PedidoResponse> mine(@org.springframework.security.core.annotation.AuthenticationPrincipal Jwt jwt) {
        return pedidos.mine(jwt.getSubject());
    }
    @GetMapping("/admin")
    public List<PedidoResponse> all() { return pedidos.all(); }
    @PatchMapping("/admin/{id}/estado")
    public PedidoResponse actualizarEstado(@PathVariable Long id,
            @jakarta.validation.Valid @RequestBody ActualizarEstadoRequest request) {
        return pedidos.actualizarEstado(id, request);
    }
}
