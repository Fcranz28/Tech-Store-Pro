package com.techstorepro.techstorepro.producto;

import java.math.BigDecimal;
import jakarta.validation.constraints.*;

public record CatalogoFiltro(
    @Size(max = 60) String categoria, @Size(max = 100) String q,
    @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal precioMin,
    @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal precioMax,
    Boolean disponible, Boolean destacado,
    @Pattern(regexp = "recientes|precio-asc|precio-desc|nombre") String orden,
    @Min(0) Integer pagina, @Min(1) @Max(48) Integer tamanio
) {}
