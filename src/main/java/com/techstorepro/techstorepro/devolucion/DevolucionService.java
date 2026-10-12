package com.techstorepro.techstorepro.devolucion;

import com.techstorepro.techstorepro.devolucion.DevolucionDtos.*;
import com.techstorepro.techstorepro.pedido.*;
import com.techstorepro.techstorepro.usuario.UsuarioRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class DevolucionService {
    private static final List<EstadoDevolucion> CONSUMIDOS = List.of(EstadoDevolucion.APROBADA, EstadoDevolucion.COMPLETADA);
    private final DevolucionRepository devoluciones;
    private final DevolucionDetalleRepository detalles;
    private final PedidoRepository pedidos;
    private final PedidoDetalleRepository lineas;
    private final UsuarioRepository usuarios;

    public DevolucionService(DevolucionRepository devoluciones, DevolucionDetalleRepository detalles,
            PedidoRepository pedidos, PedidoDetalleRepository lineas, UsuarioRepository usuarios) {
        this.devoluciones = devoluciones; this.detalles = detalles; this.pedidos = pedidos;
        this.lineas = lineas; this.usuarios = usuarios;
    }
    private Long usuarioId(String email) {
        return usuarios.findByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED)).getId();
    }
    private Pedido compraPropia(Long pedidoId, Long usuarioId) {
        Pedido pedido = pedidos.findById(pedidoId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Compra no encontrada"));
        if (!pedido.getUsuarioId().equals(usuarioId)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Compra no encontrada");
        return pedido;
    }
    @Transactional(readOnly = true)
    public List<Compra> compras(String email) {
        Long id = usuarioId(email);
        return pedidos.findByUsuarioIdOrderByCreatedAtDesc(id).stream().map(this::toCompra).toList();
    }
    @Transactional(readOnly = true)
    public Compra compra(String email, Long pedidoId) { return toCompra(compraPropia(pedidoId, usuarioId(email))); }
    private Compra toCompra(Pedido pedido) {
        List<LineaCompra> items = lineas.findByPedidoId(pedido.getId()).stream().map(linea -> new LineaCompra(
            linea.getId(), linea.getProductoId(), linea.getProductoNombre(), linea.getPrecioUnitario(),
            linea.getCantidad(), linea.getCantidad() - detalles.cantidadConsumida(linea.getId(), CONSUMIDOS))).toList();
        return new Compra(pedido.getId(), pedido.getEstado(), pedido.getCreatedAt(), items);
    }
    @Transactional
    public Respuesta solicitar(String email, Solicitud request) {
        Long userId = usuarioId(email);
        Pedido pedido = pedidos.lockById(request.pedidoId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Compra no encontrada"));
        if (!pedido.getUsuarioId().equals(userId)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Compra no encontrada");
        if (!"ENTREGADO".equals(pedido.getEstado())) throw new ResponseStatusException(HttpStatus.CONFLICT, "La compra debe estar entregada");
        Map<Long, PedidoDetalle> disponibles = lineas.findByPedidoId(pedido.getId()).stream()
            .collect(Collectors.toMap(PedidoDetalle::getId, Function.identity()));
        Set<Long> repetidos = new HashSet<>();
        BigDecimal importe = BigDecimal.ZERO;
        for (LineaSolicitud item : request.items()) {
            if (!repetidos.add(item.pedidoDetalleId())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Línea repetida");
            PedidoDetalle linea = disponibles.get(item.pedidoDetalleId());
            if (linea == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Producto ajeno a la compra");
            validarCantidad(linea, item.cantidad());
            importe = importe.add(linea.getPrecioUnitario().multiply(BigDecimal.valueOf(item.cantidad())));
        }
        Devolucion saved = devoluciones.saveAndFlush(new Devolucion(pedido.getId(), userId,
            request.motivo().trim(), request.comentario(), importe));
        detalles.saveAll(request.items().stream().map(item -> new DevolucionDetalle(saved.getId(), item.pedidoDetalleId(),
            item.cantidad(), disponibles.get(item.pedidoDetalleId()).getPrecioUnitario())).toList());
        return respuesta(saved);
    }
    private void validarCantidad(PedidoDetalle linea, int cantidad) {
        long consumida = detalles.cantidadConsumida(linea.getId(), CONSUMIDOS);
        if (cantidad < 1 || cantidad > linea.getCantidad() - consumida)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cantidad superior a la disponible para devolver");
    }
    @Transactional(readOnly = true)
    public List<Respuesta> propias(String email) {
        return devoluciones.findByUsuarioIdOrderByCreatedAtDesc(usuarioId(email)).stream().map(this::respuesta).toList();
    }
    @Transactional(readOnly = true)
    public Respuesta propia(String email, Long id) {
        Devolucion d = devoluciones.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Devolución no encontrada"));
        if (!d.getUsuarioId().equals(usuarioId(email))) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Devolución no encontrada");
        return respuesta(d);
    }
    @Transactional(readOnly = true)
    public Pagina gestion(EstadoDevolucion estado, String motivo, Instant desde, Instant hasta, int pagina, int tamanio) {
        if (desde != null && hasta != null && !desde.isBefore(hasta))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rango de fechas inválido");
        Specification<Devolucion> filtro = (root, query, cb) -> {
            List<Predicate> condiciones = new ArrayList<>();
            if (estado != null) condiciones.add(cb.equal(root.get("estado"), estado));
            if (motivo != null && !motivo.isBlank()) condiciones.add(cb.like(cb.lower(root.get("motivo")), "%" + motivo.trim().toLowerCase(Locale.ROOT) + "%"));
            if (desde != null) condiciones.add(cb.greaterThanOrEqualTo(root.get("createdAt"), desde));
            if (hasta != null) condiciones.add(cb.lessThan(root.get("createdAt"), hasta));
            return cb.and(condiciones.toArray(Predicate[]::new));
        };
        Page<Respuesta> resultado = devoluciones.findAll(filtro,
            PageRequest.of(Math.max(0, pagina), Math.max(1, Math.min(50, tamanio)), Sort.by(Sort.Direction.DESC, "createdAt")))
            .map(this::respuesta);
        return new Pagina(resultado.getContent(), resultado.getTotalElements(), resultado.getTotalPages(), resultado.getNumber());
    }
    @Transactional(readOnly = true)
    public Respuesta gestionDetalle(Long id) {
        return respuesta(devoluciones.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Devolución no encontrada")));
    }
    @Transactional
    public Respuesta cambiar(Long id, CambioEstado request) {
        Devolucion d = devoluciones.lockById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Devolución no encontrada"));
        if (d.getEstado() != request.estadoActual()) throw new ResponseStatusException(HttpStatus.CONFLICT, "El estado cambió; actualiza la solicitud");
        if (!d.getEstado().puedeAvanzarA(request.estado())) throw new ResponseStatusException(HttpStatus.CONFLICT, "Transición de estado no permitida");
        if (request.estado() == EstadoDevolucion.RECHAZADA && (request.observacionOperador() == null || request.observacionOperador().isBlank()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Explica el motivo del rechazo");
        if (request.estado() == EstadoDevolucion.APROBADA) {
            // Serializa aprobaciones de la misma compra para evitar exceder las unidades compradas.
            pedidos.lockById(d.getPedidoId());
            Map<Long, PedidoDetalle> compra = lineas.findByPedidoId(d.getPedidoId()).stream()
                .collect(Collectors.toMap(PedidoDetalle::getId, Function.identity()));
            for (DevolucionDetalle item : detalles.findByDevolucionIdIn(List.of(id)))
                validarCantidad(compra.get(item.getPedidoDetalleId()), item.getCantidad());
        }
        d.avanzar(request.estado(), request.observacionOperador());
        return respuesta(d);
    }
    private Respuesta respuesta(Devolucion d) {
        Map<Long, PedidoDetalle> compra = lineas.findByPedidoId(d.getPedidoId()).stream()
            .collect(Collectors.toMap(PedidoDetalle::getId, Function.identity()));
        List<Linea> items = detalles.findByDevolucionIdIn(List.of(d.getId())).stream().map(item -> {
            PedidoDetalle linea = compra.get(item.getPedidoDetalleId());
            return new Linea(item.getPedidoDetalleId(), linea.getProductoNombre(), item.getCantidad(),
                item.getPrecioUnitario(), item.getSubtotal());
        }).toList();
        return new Respuesta(d.getId(), d.getPedidoId(), d.getUsuarioId(), d.getEstado(), d.getMotivo(), d.getComentario(),
            d.getObservacionOperador(), d.getImporte(), d.getCreatedAt(), d.getUpdatedAt(), items);
    }
}
