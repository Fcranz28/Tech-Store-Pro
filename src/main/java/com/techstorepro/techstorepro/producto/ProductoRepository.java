package com.techstorepro.techstorepro.producto;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface ProductoRepository extends JpaRepository<Producto, Long>, org.springframework.data.jpa.repository.JpaSpecificationExecutor<Producto> {
    interface CategoriaResumen { String getCategoria(); long getCantidad(); }
    @Query("select p.categoria as categoria, count(p) as cantidad from Producto p where p.activo = true group by p.categoria order by p.categoria")
    List<CategoriaResumen> categoriasCatalogo();
    List<Producto> findByActivoTrueOrderByIdDesc();
    List<Producto> findAllByOrderByIdDesc();
    Optional<Producto> findByIdAndActivoTrue(Long id);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Producto p where p.id = :id")
    Optional<Producto> lockById(@Param("id") Long id);
}
