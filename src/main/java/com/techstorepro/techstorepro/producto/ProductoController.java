package com.techstorepro.techstorepro.producto;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/productos")
public class ProductoController {
    private final ProductoService productos;
    public ProductoController(ProductoService productos) { this.productos = productos; }

    @GetMapping
    public List<ProductoResponse> list() {
        return productos.list();
    }
    @GetMapping("/{id}")
    public ProductoResponse get(@PathVariable Long id) {
        return productos.get(id);
    }
    @GetMapping("/admin")
    public List<ProductoResponse> listAdmin() {
        return productos.listAdmin();
    }
    @GetMapping("/catalogo")
    public CatalogoResponse catalogo(@Valid @ModelAttribute CatalogoFiltro filtro) { return productos.catalogo(filtro); }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductoResponse create(@Valid @RequestBody ProductoRequest request) {
        return productos.create(request);
    }
    @PutMapping("/{id}")
    public ProductoResponse update(@PathVariable Long id, @Valid @RequestBody ProductoRequest request) {
        return productos.update(id, request);
    }
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        productos.delete(id);
    }
}
