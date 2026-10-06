package com.techstorepro.techstorepro.pedido;

import com.techstorepro.techstorepro.carrito.CarritoItem;
import com.techstorepro.techstorepro.carrito.CarritoItemRepository;
import com.techstorepro.techstorepro.producto.Producto;
import com.techstorepro.techstorepro.producto.ProductoRepository;
import com.techstorepro.techstorepro.usuario.UsuarioRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PedidoService {
    private final PedidoRepository pedidos;
    private final PedidoDetalleRepository detalles;
    private final CarritoItemRepository carrito;
    private final ProductoRepository productos;
    private final UsuarioRepository usuarios;
    public PedidoService(PedidoRepository pedidos, PedidoDetalleRepository detalles, CarritoItemRepository carrito,
            ProductoRepository productos, UsuarioRepository usuarios) {
        this.pedidos = pedidos; this.detalles = detalles; this.carrito = carrito;
        this.productos = productos; this.usuarios = usuarios;
    }
    private Long userId(String email) {
        return usuarios.findByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED)).getId();
    }
    @Transactional
    public PedidoResponse confirm(String email) {
        Long userId = usuarios.lockByEmail(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED)).getId();
        List<CarritoItem> cart = carrito.findByUsuarioIdOrderById(userId);
        if (cart.isEmpty()) throw new ResponseStatusException(HttpStatus.CONFLICT, "El carrito está vacío");
        Map<Long, Producto> locked = cart.stream().map(CarritoItem::getProductoId).distinct().sorted()
            .collect(Collectors.toMap(id -> id, id -> productos.lockById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Producto no disponible"))));
        BigDecimal total = BigDecimal.ZERO;
        for (CarritoItem item : cart) {
            Producto p = locked.get(item.getProductoId());
            if (!p.isActivo() || item.getCantidad() > p.getStock()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Stock insuficiente o producto no disponible: " + p.getNombre());
            }
            total = total.add(p.getPrecio().multiply(BigDecimal.valueOf(item.getCantidad())));
        }
        Pedido pedido = pedidos.saveAndFlush(new Pedido(userId, total));
        List<PedidoDetalle> lines = cart.stream().map(item -> {
            Producto p = locked.get(item.getProductoId());
            p.decreaseStock(item.getCantidad());
            return new PedidoDetalle(pedido.getId(), p.getId(), p.getNombre(), p.getPrecio(), item.getCantidad());
        }).toList();
        detalles.saveAll(lines);
        carrito.deleteByUsuarioId(userId);
        return toResponse(pedido, lines);
    }
    @Transactional(readOnly = true)
    public List<PedidoResponse> mine(String email) { return mapOrders(pedidos.findByUsuarioIdOrderByCreatedAtDesc(userId(email))); }
    @Transactional(readOnly = true)
    public List<PedidoResponse> all() { return mapOrders(pedidos.findAllByOrderByCreatedAtDesc()); }
    @Transactional
    public PedidoResponse actualizarEstado(Long id, ActualizarEstadoRequest request) {
        Pedido pedido = pedidos.lockById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado"));
        EstadoPedido actual = EstadoPedido.valueOf(pedido.getEstado());
        if (actual != request.estadoActual()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El estado cambió. Actualiza los pedidos e inténtalo de nuevo.");
        }
        if (!actual.puedeAvanzarA(request.estado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Solo se permite avanzar al siguiente estado del pedido.");
        }
        pedido.avanzarEstado(request.estado());
        return toResponse(pedido, detalles.findByPedidoIdIn(List.of(id)));
    }
    private List<PedidoResponse> mapOrders(List<Pedido> orders) {
        if (orders.isEmpty()) return List.of();
        Map<Long, List<PedidoDetalle>> byOrder = detalles.findByPedidoIdIn(orders.stream().map(Pedido::getId).toList())
            .stream().collect(Collectors.groupingBy(PedidoDetalle::getPedidoId));
        return orders.stream().map(p -> toResponse(p, byOrder.getOrDefault(p.getId(), List.of()))).toList();
    }
    private PedidoResponse toResponse(Pedido p, List<PedidoDetalle> lines) {
        return new PedidoResponse(p.getId(), p.getUsuarioId(), p.getEstado(), p.getTotal(), p.getCreatedAt(),
            lines.stream().map(PedidoResponse.Linea::from).toList());
    }
}
