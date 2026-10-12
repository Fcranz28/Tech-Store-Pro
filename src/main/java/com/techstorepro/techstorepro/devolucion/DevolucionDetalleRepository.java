package com.techstorepro.techstorepro.devolucion;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DevolucionDetalleRepository extends JpaRepository<DevolucionDetalle, Long> {
    List<DevolucionDetalle> findByDevolucionIdIn(Collection<Long> ids);
    @Query("select coalesce(sum(dd.cantidad), 0) from DevolucionDetalle dd, Devolucion d where dd.devolucionId = d.id and dd.pedidoDetalleId = :lineaId and d.estado in :estados")
    long cantidadConsumida(@Param("lineaId") Long lineaId, @Param("estados") Collection<EstadoDevolucion> estados);
}
