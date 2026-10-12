package com.techstorepro.techstorepro.devolucion;

public enum EstadoDevolucion {
    SOLICITADA, EN_REVISION, APROBADA, RECHAZADA, COMPLETADA;

    public boolean puedeAvanzarA(EstadoDevolucion siguiente) {
        return switch (this) {
            case SOLICITADA -> siguiente == EN_REVISION;
            case EN_REVISION -> siguiente == APROBADA || siguiente == RECHAZADA;
            case APROBADA -> siguiente == COMPLETADA;
            default -> false;
        };
    }
}
