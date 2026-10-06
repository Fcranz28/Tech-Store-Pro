package com.techstorepro.techstorepro.producto;

import java.util.List;

public record CatalogoResponse(List<ProductoResponse> productos, long total, int pagina, int totalPaginas,
    int tamanio, long totalCatalogo, List<Categoria> categorias) {
    public record Categoria(String value, String label, long count) {}
}
