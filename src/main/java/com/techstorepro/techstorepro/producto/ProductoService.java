package com.techstorepro.techstorepro.producto;

import java.util.List;
import java.util.Locale;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProductoService {
    private final ProductoRepository productos;
    public ProductoService(ProductoRepository productos) { this.productos = productos; }

    @Transactional(readOnly = true, isolation = org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public CatalogoResponse catalogo(CatalogoFiltro filtro) {
        if (filtro.precioMin() != null && filtro.precioMax() != null && filtro.precioMin().compareTo(filtro.precioMax()) > 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El precio mínimo no puede superar al máximo.");
        String orden = filtro.orden() == null ? "recientes" : filtro.orden();
        Sort sort = switch (orden) {
            case "precio-asc" -> Sort.by("precio").ascending();
            case "precio-desc" -> Sort.by("precio").descending();
            case "nombre" -> Sort.by("nombre").ascending();
            default -> Sort.by("id").descending();
        };
        int pagina = filtro.pagina() == null ? 0 : filtro.pagina();
        int tamanio = filtro.tamanio() == null ? 12 : filtro.tamanio();
        var page = productos.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isTrue(root.get("activo")));
            if (filtro.categoria() != null && !filtro.categoria().isBlank())
                predicates.add(cb.equal(root.get("categoria"), filtro.categoria().trim().toUpperCase(Locale.ROOT)));
            if (filtro.q() != null && !filtro.q().isBlank()) {
                String term = filtro.q().trim().toLowerCase(Locale.ROOT).replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
                predicates.add(cb.or(cb.like(cb.lower(root.get("nombre")), "%" + term + "%", '\\'),
                    cb.like(cb.lower(root.get("descripcion")), "%" + term + "%", '\\')));
            }
            if (filtro.precioMin() != null) predicates.add(cb.greaterThanOrEqualTo(root.get("precio"), filtro.precioMin()));
            if (filtro.precioMax() != null) predicates.add(cb.lessThanOrEqualTo(root.get("precio"), filtro.precioMax()));
            if (filtro.disponible() != null) predicates.add(filtro.disponible() ? cb.greaterThan(root.get("stock"), 0) : cb.equal(root.get("stock"), 0));
            if (filtro.destacado() != null) predicates.add(cb.equal(root.get("destacado"), filtro.destacado()));
            return cb.and(predicates.toArray(Predicate[]::new));
        }, PageRequest.of(pagina, tamanio, sort.and(Sort.by("id").descending())));
        var categorias = productos.categoriasCatalogo().stream().map(c -> new CatalogoResponse.Categoria(c.getCategoria(),
            switch (c.getCategoria()) { case "AUDIFONOS" -> "Audífonos"; case "TECLADOS" -> "Teclados"; case "MOUSE" -> "Mouse";
                case "AUDIO" -> "Audio"; case "ACCESORIOS" -> "Accesorios"; default -> c.getCategoria(); }, c.getCantidad())).toList();
        return new CatalogoResponse(page.getContent().stream().map(ProductoResponse::from).toList(), page.getTotalElements(),
            page.getNumber(), page.getTotalPages(), page.getSize(), categorias.stream().mapToLong(CatalogoResponse.Categoria::count).sum(), categorias);
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> list() {
        return productos.findByActivoTrueOrderByIdDesc().stream().map(ProductoResponse::from).toList();
    }
    @Transactional(readOnly = true)
    public ProductoResponse get(Long id) {
        return productos.findByIdAndActivoTrue(id).map(ProductoResponse::from)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
    }
    @Transactional(readOnly = true)
    public List<ProductoResponse> listAdmin() {
        return productos.findAllByOrderByIdDesc().stream().map(ProductoResponse::from).toList();
    }
    @Transactional
    public ProductoResponse create(ProductoRequest request) {
        return ProductoResponse.from(productos.save(new Producto(request)));
    }
    @Transactional
    public ProductoResponse update(Long id, ProductoRequest request) {
        Producto p = productos.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
        p.update(request);
        return ProductoResponse.from(p);
    }
    @Transactional
    public void delete(Long id) {
        Producto p = productos.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
        p.deactivate();
    }
}
