package com.techstorepro.techstorepro.devolucion;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface DevolucionRepository extends JpaRepository<Devolucion, Long>, JpaSpecificationExecutor<Devolucion> {
    List<Devolucion> findByUsuarioIdOrderByCreatedAtDesc(Long usuarioId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from Devolucion d where d.id = :id")
    Optional<Devolucion> lockById(@Param("id") Long id);
}
