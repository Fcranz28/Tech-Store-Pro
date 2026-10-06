package com.techstorepro.techstorepro.pedido;

import jakarta.validation.constraints.NotNull;

public record ActualizarEstadoRequest(@NotNull EstadoPedido estadoActual, @NotNull EstadoPedido estado) {}
