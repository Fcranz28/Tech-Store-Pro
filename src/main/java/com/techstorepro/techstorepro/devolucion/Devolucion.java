package com.techstorepro.techstorepro.devolucion;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "devoluciones")
public class Devolucion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "pedido_id", nullable = false) private Long pedidoId;
    @Column(name = "usuario_id", nullable = false) private Long usuarioId;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private EstadoDevolucion estado = EstadoDevolucion.SOLICITADA;
    @Column(nullable = false, length = 40) private String motivo;
    @Column(length = 1000) private String comentario;
    @Column(name = "observacion_operador", length = 1000) private String observacionOperador;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal importe;
    @Column(name = "created_at", nullable = false) private Instant createdAt = Instant.now();
    @Column(name = "updated_at", nullable = false) private Instant updatedAt = Instant.now();

    protected Devolucion() {}
    public Devolucion(Long pedidoId, Long usuarioId, String motivo, String comentario, BigDecimal importe) {
        this.pedidoId = pedidoId; this.usuarioId = usuarioId; this.motivo = motivo;
        this.comentario = comentario; this.importe = importe;
    }
    public void avanzar(EstadoDevolucion siguiente, String observacion) {
        this.estado = siguiente;
        if (observacion != null && !observacion.isBlank()) this.observacionOperador = observacion.trim();
        this.updatedAt = Instant.now();
    }
    public Long getId() { return id; }
    public Long getPedidoId() { return pedidoId; }
    public Long getUsuarioId() { return usuarioId; }
    public EstadoDevolucion getEstado() { return estado; }
    public String getMotivo() { return motivo; }
    public String getComentario() { return comentario; }
    public String getObservacionOperador() { return observacionOperador; }
    public BigDecimal getImporte() { return importe; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
