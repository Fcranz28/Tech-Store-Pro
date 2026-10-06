package com.techstorepro.techstorepro.pedido;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select p from Pedido p where p.id = :id")
    java.util.Optional<Pedido> lockById(@org.springframework.data.repository.query.Param("id") Long id);
    List<Pedido> findByUsuarioIdOrderByCreatedAtDesc(Long usuarioId);
    List<Pedido> findAllByOrderByCreatedAtDesc();
}
