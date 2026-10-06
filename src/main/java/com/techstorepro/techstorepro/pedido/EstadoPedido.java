package com.techstorepro.techstorepro.pedido;

public enum EstadoPedido {
    CONFIRMADO, EN_PREPARACION, ENVIADO, ENTREGADO;

    public boolean puedeAvanzarA(EstadoPedido siguiente) {
        return ordinal() < values().length - 1 && siguiente.ordinal() == ordinal() + 1;
    }
}
