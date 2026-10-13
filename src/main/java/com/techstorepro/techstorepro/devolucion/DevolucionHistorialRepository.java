package com.techstorepro.techstorepro.devolucion;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DevolucionHistorialRepository extends JpaRepository<DevolucionHistorial, Long> {
    List<DevolucionHistorial> findByDevolucionIdOrderByCreatedAtAscIdAsc(Long devolucionId);
}
