package com.techstorepro.techstorepro.devolucion;

import jakarta.persistence.*;
import java.time.Instant;
import com.techstorepro.techstorepro.usuario.Usuario;

@Entity
@Table(name = "devolucion_historial")
public class DevolucionHistorial {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "devolucion_id", nullable = false) private Long devolucionId;
    @Enumerated(EnumType.STRING) @Column(name = "estado_anterior", length = 20) private EstadoDevolucion estadoAnterior;
    @Enumerated(EnumType.STRING) @Column(name = "estado_nuevo", nullable = false, length = 20) private EstadoDevolucion estadoNuevo;
    @Column(name = "actor_id") private Long actorId;
    @Column(name = "actor_nombre", nullable = false, length = 140) private String actorNombre;
    @Column(name = "actor_rol", nullable = false, length = 20) private String actorRol;
    @Column(length = 1000) private String observacion;
    @Column(name = "created_at", nullable = false) private Instant createdAt = Instant.now();
    protected DevolucionHistorial() {}
    public DevolucionHistorial(Long devolucionId, EstadoDevolucion anterior, EstadoDevolucion nuevo, Usuario actor, String observacion) {
        this.devolucionId = devolucionId; this.estadoAnterior = anterior; this.estadoNuevo = nuevo;
        actorId = actor.getId(); actorNombre = actor.getNombre(); actorRol = actor.getRol().name();
        this.observacion = observacion == null || observacion.isBlank() ? null : observacion.trim();
    }
    public Long getId() { return id; }
    public EstadoDevolucion getEstadoAnterior() { return estadoAnterior; }
    public EstadoDevolucion getEstadoNuevo() { return estadoNuevo; }
    public Long getActorId() { return actorId; }
    public String getActorNombre() { return actorNombre; }
    public String getActorRol() { return actorRol; }
    public String getObservacion() { return observacion; }
    public Instant getCreatedAt() { return createdAt; }
}
