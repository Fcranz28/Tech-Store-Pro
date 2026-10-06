package com.techstorepro.techstorepro.carrito;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarritoItemRepository extends JpaRepository<CarritoItem, Long> {
    List<CarritoItem> findByUsuarioIdOrderById(Long usuarioId);
    Optional<CarritoItem> findByUsuarioIdAndProductoId(Long usuarioId, Long productoId);
    Optional<CarritoItem> findByIdAndUsuarioId(Long id, Long usuarioId);
    void deleteByUsuarioId(Long usuarioId);
}
