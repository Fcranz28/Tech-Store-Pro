package com.techstorepro.techstorepro.devolucion;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class DevolucionDtos {
    private DevolucionDtos() {}
    public record LineaSolicitud(@NotNull Long pedidoDetalleId, @Min(1) int cantidad) {}
    public record Solicitud(@NotNull Long pedidoId, @NotBlank @Size(max = 40) String motivo,
        @Size(max = 1000) String comentario, @NotEmpty List<@NotNull @Valid LineaSolicitud> items) {}
    public record CambioEstado(@NotNull EstadoDevolucion estadoActual, @NotNull EstadoDevolucion estado,
        @Size(max = 1000) String observacionOperador) {}
    public record LineaCompra(Long pedidoDetalleId, Long productoId, String nombre, BigDecimal precioUnitario,
        int comprado, long disponible) {}
    public record Compra(Long id, String estado, Instant createdAt, List<LineaCompra> items) {}
    public record Linea(Long pedidoDetalleId, String nombre, int cantidad, BigDecimal precioUnitario, BigDecimal subtotal) {}
    public record Respuesta(Long id, Long pedidoId, Long usuarioId, EstadoDevolucion estado, String motivo,
        String comentario, String observacionOperador, BigDecimal importe, Instant createdAt, Instant updatedAt,
        List<Linea> items, List<Evento> historial) {}
    public record Evento(Long id, EstadoDevolucion estadoAnterior, EstadoDevolucion estadoNuevo,
        Long actorId, String actorNombre, String actorRol, String observacion, Instant createdAt) {
        public static Evento from(DevolucionHistorial h) {
            return new Evento(h.getId(), h.getEstadoAnterior(), h.getEstadoNuevo(), h.getActorId(),
                h.getActorNombre(), h.getActorRol(), h.getObservacion(), h.getCreatedAt());
        }
    }
    public record Pagina(List<Respuesta> content, long totalElements, int totalPages, int number) {}
}
