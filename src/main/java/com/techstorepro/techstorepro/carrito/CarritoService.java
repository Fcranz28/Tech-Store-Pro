package com.techstorepro.techstorepro.carrito;

import com.techstorepro.techstorepro.producto.Producto;
import com.techstorepro.techstorepro.producto.ProductoRepository;
import com.techstorepro.techstorepro.usuario.UsuarioRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CarritoService {
    private final CarritoItemRepository items;
    private final ProductoRepository productos;
    private final UsuarioRepository usuarios;
    public CarritoService(CarritoItemRepository items, ProductoRepository productos, UsuarioRepository usuarios) {
        this.items = items; this.productos = productos; this.usuarios = usuarios;
    }
    public Long userId(String email) {
        return usuarios.findByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED)).getId();
    }
    @Transactional(readOnly = true)
    public CarritoResponse get(String email) {
        List<CarritoItem> cart = items.findByUsuarioIdOrderById(userId(email));
        Map<Long, Producto> byId = productos.findAllById(cart.stream().map(CarritoItem::getProductoId).toList())
            .stream().collect(Collectors.toMap(Producto::getId, Function.identity()));
        List<CarritoResponse.Item> lines = cart.stream().map(item -> {
            Producto p = byId.get(item.getProductoId());
            if (p == null) throw new ResponseStatusException(HttpStatus.CONFLICT, "Un producto del carrito ya no existe");
            return new CarritoResponse.Item(item.getId(), p.getId(), p.getNombre(), p.getCategoria(), p.getImagenUrl(),
                p.getPrecio(), p.getStock(), item.getCantidad(), p.getPrecio().multiply(BigDecimal.valueOf(item.getCantidad())));
        }).toList();
        BigDecimal total = lines.stream().map(CarritoResponse.Item::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CarritoResponse(lines, total);
    }
    @Transactional
    public CarritoResponse add(String email, Long productoId, int cantidad) {
        Long userId = userId(email);
        Producto p = productos.findByIdAndActivoTrue(productoId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no disponible"));
        CarritoItem item = items.findByUsuarioIdAndProductoId(userId, productoId).orElse(null);
        int requested = cantidad + (item == null ? 0 : item.getCantidad());
        requireStock(p, requested);
        if (item == null) items.save(new CarritoItem(userId, productoId, requested));
        else item.setCantidad(requested);
        return get(email);
    }
    @Transactional
    public CarritoResponse update(String email, Long itemId, int cantidad) {
        CarritoItem item = ownedItem(email, itemId);
        Producto p = productos.findByIdAndActivoTrue(item.getProductoId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Producto no disponible"));
        requireStock(p, cantidad);
        item.setCantidad(cantidad);
        return get(email);
    }
    @Transactional
    public void remove(String email, Long itemId) { items.delete(ownedItem(email, itemId)); }
    private CarritoItem ownedItem(String email, Long itemId) {
        return items.findByIdAndUsuarioId(itemId, userId(email))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Artículo no encontrado"));
    }
    private void requireStock(Producto p, int requested) {
        if (requested > p.getStock()) throw new ResponseStatusException(HttpStatus.CONFLICT, "Stock insuficiente");
    }
}
