package pe.edu.upeu.bomerp.articulo.web.dto;

import pe.edu.upeu.bomerp.articulo.model.Articulo;

/**
 * Vista de solo lectura que el Portal Web consume para el catalogo
 * publico (visitante anonimo). Deliberadamente NO incluye
 * {@code costoReferencial} ni ninguna cantidad de stock -- Inventario es
 * quien sabe el stock exacto, y ese dato nunca se muestra a un visitante
 * anonimo (requisito del modulo Portal: "catalogo publico de solo
 * lectura ... nombre, descripcion e imagen").
 */
public record ArticuloPublicoResponse(
        String codigo,
        String nombre,
        String descripcion,
        String imagenUrl
) {
    public static ArticuloPublicoResponse desde(Articulo articulo) {
        return new ArticuloPublicoResponse(
                articulo.getCodigo(),
                articulo.getNombre(),
                articulo.getDescripcion(),
                articulo.getImagenUrl());
    }
}
