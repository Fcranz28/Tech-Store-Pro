package com.techstorepro.techstorepro.carrito;

import com.techstorepro.techstorepro.producto.Producto;
import com.techstorepro.techstorepro.producto.ProductoRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CarritoInvitadoService {
    public record InvitadoResponse(String token, CarritoResponse carrito) {}
    private static final int MAX_ITEMS = 30;
    private final ProductoRepository productos;
    private final CarritoService carrito;
    private final JwtEncoder encoder;
    private final JwtDecoder decoder;
    private final String issuer;

    public CarritoInvitadoService(ProductoRepository productos, CarritoService carrito, JwtEncoder encoder,
            JwtDecoder decoder, @Value("${app.jwt.issuer}") String issuer) {
        this.productos = productos; this.carrito = carrito; this.encoder = encoder; this.decoder = decoder; this.issuer = issuer;
    }

    public InvitadoResponse current(String token) { return response(read(token)); }

    public InvitadoResponse add(String token, Long productId, int quantity) {
        Map<Long, Integer> lines = read(token);
        Producto product = available(productId);
        int requested = lines.getOrDefault(productId, 0) + quantity;
        requireStock(product, requested);
        if (!lines.containsKey(productId) && lines.size() >= MAX_ITEMS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Máximo de productos en el carrito");
        }
        lines.put(productId, requested);
        return response(lines);
    }

    public InvitadoResponse update(String token, Long productId, int quantity) {
        Map<Long, Integer> lines = read(token);
        if (!lines.containsKey(productId)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        requireStock(available(productId), quantity);
        lines.put(productId, quantity);
        return response(lines);
    }

    public InvitadoResponse remove(String token, Long productId) {
        Map<Long, Integer> lines = read(token);
        lines.remove(productId);
        return response(lines);
    }

    @Transactional
    public CarritoResponse transfer(String email, String token) {
        Map<Long, Integer> lines = read(token);
        for (var line : lines.entrySet()) {
            Producto product = available(line.getKey());
            CarritoResponse existing = carrito.get(email);
            int current = existing.items().stream().filter(item -> item.productoId().equals(line.getKey()))
                .mapToInt(CarritoResponse.Item::cantidad).findFirst().orElse(0);
            int target = Math.max(current, line.getValue());
            requireStock(product, target);
            if (target > current) carrito.add(email, line.getKey(), target - current);
        }
        return carrito.get(email);
    }

    private Map<Long, Integer> read(String token) {
        Map<Long, Integer> lines = new LinkedHashMap<>();
        if (token == null || token.isBlank()) return lines;
        Jwt jwt;
        try { jwt = decoder.decode(token); }
        catch (JwtException exception) { throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Carrito invitado vencido"); }
        if (!"GUEST".equals(jwt.getClaimAsString("role")) || !"cart".equals(jwt.getClaimAsString("purpose"))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token de carrito inválido");
        }
        Object claim = jwt.getClaim("items");
        if (!(claim instanceof List<?> values) || values.size() > MAX_ITEMS) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Carrito invitado inválido");
        }
        for (Object value : values) {
            if (!(value instanceof Map<?, ?> item) || !(item.get("id") instanceof Number id)
                    || !(item.get("qty") instanceof Number qty) || id.longValue() <= 0
                    || qty.intValue() < 1 || qty.intValue() > 1000) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Carrito invitado inválido");
            }
            lines.put(id.longValue(), qty.intValue());
        }
        return lines;
    }

    private InvitadoResponse response(Map<Long, Integer> lines) {
        List<CarritoResponse.Item> items = new ArrayList<>();
        for (var line : lines.entrySet()) {
            Producto product = available(line.getKey());
            items.add(new CarritoResponse.Item(product.getId(), product.getId(), product.getNombre(),
                product.getCategoria(), product.getImagenUrl(), product.getPrecio(), product.getStock(),
                line.getValue(), product.getPrecio().multiply(BigDecimal.valueOf(line.getValue()))));
        }
        BigDecimal total = items.stream().map(CarritoResponse.Item::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        Instant now = Instant.now();
        List<Map<String, Number>> claim = lines.entrySet().stream()
            .map(item -> Map.<String, Number>of("id", item.getKey(), "qty", item.getValue())).toList();
        JwtClaimsSet claims = JwtClaimsSet.builder().issuer(issuer).subject("guest:" + UUID.randomUUID())
            .issuedAt(now).expiresAt(now.plus(7, ChronoUnit.DAYS)).claim("role", "GUEST")
            .claim("purpose", "cart").claim("items", claim).build();
        String signed = encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        return new InvitadoResponse(signed, new CarritoResponse(items, total));
    }

    private Producto available(Long productId) {
        return productos.findByIdAndActivoTrue(productId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Producto no disponible"));
    }

    private void requireStock(Producto product, int quantity) {
        if (quantity > product.getStock()) throw new ResponseStatusException(HttpStatus.CONFLICT, "Stock insuficiente");
    }
}
