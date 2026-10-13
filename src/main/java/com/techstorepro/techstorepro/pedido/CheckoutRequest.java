package com.techstorepro.techstorepro.pedido;

import jakarta.validation.constraints.*;

public record CheckoutRequest(
    @NotBlank @Size(max = 140) String nombreDestinatario,
    @NotBlank @Pattern(regexp = "[0-9+ ()-]{7,20}") String telefonoEntrega,
    @NotBlank @Size(max = 300) String direccionEntrega
) {}
