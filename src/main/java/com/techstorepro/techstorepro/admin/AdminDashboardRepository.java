package com.techstorepro.techstorepro.admin;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AdminDashboardRepository {
    private final JdbcTemplate jdbc;
    public AdminDashboardRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public record TotalesPedidos(BigDecimal importe, long clientes, long pedidos, long pendientes) {}
    public record TotalesProductos(long total, long visibles, long conStock, long agotados) {}

    public TotalesPedidos totalesPedidos() {
        return jdbc.queryForObject("""
            select coalesce(sum(total), 0) importe, count(distinct usuario_id) clientes,
                   count(*) pedidos, count(*) filter (where estado <> 'ENTREGADO') pendientes
            from pedidos
            """, (rs, row) -> new TotalesPedidos(rs.getBigDecimal("importe"), rs.getLong("clientes"),
                rs.getLong("pedidos"), rs.getLong("pendientes")));
    }

    public TotalesProductos totalesProductos() {
        return jdbc.queryForObject("""
            select count(*) total, count(*) filter (where activo) visibles,
                   count(*) filter (where activo and stock > 0) con_stock,
                   count(*) filter (where activo and stock = 0) agotados from productos
            """, (rs, row) -> new TotalesProductos(rs.getLong("total"), rs.getLong("visibles"),
                rs.getLong("con_stock"), rs.getLong("agotados")));
    }

    public Map<String, Long> pedidosPorEstado() {
        return jdbc.query("select estado, count(*) cantidad from pedidos group by estado",
            (rs, row) -> Map.entry(rs.getString("estado"), rs.getLong("cantidad")))
            .stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public List<AdminDashboardResponse.PedidoReciente> pedidosRecientes() {
        return jdbc.query("""
            select id, usuario_id, estado, total, created_at from pedidos
            order by created_at desc, id desc limit 5
            """, (rs, row) -> new AdminDashboardResponse.PedidoReciente(rs.getLong("id"),
                rs.getLong("usuario_id"), rs.getString("estado"), rs.getBigDecimal("total"),
                rs.getTimestamp("created_at").toInstant()));
    }

    public List<AdminDashboardResponse.ProductoPopular> productosMasPedidos() {
        return jdbc.query("""
            select d.producto_id, coalesce(p.nombre, max(d.producto_nombre)) nombre,
                   sum(d.cantidad) unidades, sum(d.subtotal) importe
            from pedido_detalles d join pedidos o on o.id = d.pedido_id
            left join productos p on p.id = d.producto_id
            group by d.producto_id, p.nombre
            order by unidades desc, importe desc, d.producto_id asc limit 4
            """, (rs, row) -> new AdminDashboardResponse.ProductoPopular(rs.getLong("producto_id"),
                rs.getString("nombre"), rs.getLong("unidades"), rs.getBigDecimal("importe")));
    }
}
