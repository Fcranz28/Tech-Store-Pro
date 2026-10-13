package com.techstorepro.techstorepro.devolucion;

import com.techstorepro.techstorepro.devolucion.DevolucionDtos.*;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/devoluciones")
public class DevolucionController {
    private final DevolucionService service;
    public DevolucionController(DevolucionService service) { this.service = service; }

    @GetMapping("/compras") public List<Compra> compras(@AuthenticationPrincipal Jwt jwt) { return service.compras(jwt.getSubject()); }
    @GetMapping("/compras/{pedidoId}") public Compra compra(@AuthenticationPrincipal Jwt jwt, @PathVariable Long pedidoId) {
        return service.compra(jwt.getSubject(), pedidoId);
    }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public Respuesta solicitar(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody Solicitud request) {
        return service.solicitar(jwt.getSubject(), request);
    }
    @GetMapping public List<Respuesta> propias(@AuthenticationPrincipal Jwt jwt) { return service.propias(jwt.getSubject()); }
    @GetMapping("/{id}") public Respuesta propia(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return service.propia(jwt.getSubject(), id);
    }
    @GetMapping("/gestion") public Pagina gestion(@RequestParam(required = false) EstadoDevolucion estado,
            @RequestParam(required = false) String motivo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant hasta,
            @RequestParam(defaultValue = "0") int pagina, @RequestParam(defaultValue = "10") int tamanio) {
        return service.gestion(estado, motivo, desde, hasta, pagina, tamanio);
    }
    @GetMapping("/gestion/{id}") public Respuesta gestionDetalle(@PathVariable Long id) { return service.gestionDetalle(id); }
    @PatchMapping("/gestion/{id}/estado") public Respuesta cambiar(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id, @Valid @RequestBody CambioEstado request) {
        return service.cambiar(jwt.getSubject(), id, request);
    }
}
