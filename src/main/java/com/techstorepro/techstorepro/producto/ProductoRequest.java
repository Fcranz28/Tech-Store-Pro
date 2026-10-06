package com.techstorepro.techstorepro.producto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ProductoRequest(
    @NotBlank @Size(max = 140) String nombre,
    @NotBlank String descripcion,
    @NotBlank @Size(max = 60) String categoria,
    @NotNull @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal precio,
    @Min(0) int stock,
    @Size(max = 500) String imagenUrl,
    boolean destacado,
    boolean activo
) {}
