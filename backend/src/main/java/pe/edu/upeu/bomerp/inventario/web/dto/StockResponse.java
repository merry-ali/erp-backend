package pe.edu.upeu.bomerp.inventario.web.dto;

import java.math.BigDecimal;

public record StockResponse(
        Long almacenId,
        String codigoArticulo,
        BigDecimal cantidadDisponible
) {
}
