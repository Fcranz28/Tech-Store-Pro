package com.techstorepro.techstorepro.pedido;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "pedidos")
public class Pedido {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "usuario_id", nullable = false) private Long usuarioId;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal total;
    @Column(nullable = false, length = 30) private String estado = "CONFIRMADO";
    @Column(name = "created_at", nullable = false) private Instant createdAt = Instant.now();
    @Column(name = "nombre_destinatario", length = 140) private String nombreDestinatario;
    @Column(name = "telefono_entrega", length = 20) private String telefonoEntrega;
    @Column(name = "direccion_entrega", length = 300) private String direccionEntrega;
    @Column(name = "estado_pago", nullable = false, length = 20) private String estadoPago = "NO_PROCESADO";
    protected Pedido() {}
    public Pedido(Long usuarioId, BigDecimal total) { this.usuarioId = usuarioId; this.total = total; }
    public Long getId() { return id; }
    public Long getUsuarioId() { return usuarioId; }
    public BigDecimal getTotal() { return total; }
    public String getEstado() { return estado; }
    public void avanzarEstado(EstadoPedido siguiente) { this.estado = siguiente.name(); }
    public Instant getCreatedAt() { return createdAt; }
    public void checkoutSimulado(CheckoutRequest request) {
        nombreDestinatario = request.nombreDestinatario().trim();
        telefonoEntrega = request.telefonoEntrega().trim();
        direccionEntrega = request.direccionEntrega().trim();
        estadoPago = "SIMULADO";
    }
    public String getNombreDestinatario() { return nombreDestinatario; }
    public String getTelefonoEntrega() { return telefonoEntrega; }
    public String getDireccionEntrega() { return direccionEntrega; }
    public String getEstadoPago() { return estadoPago; }
}
