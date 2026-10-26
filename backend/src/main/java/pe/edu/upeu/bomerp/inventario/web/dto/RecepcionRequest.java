package pe.edu.upeu.bomerp.inventario.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record RecepcionRequest(
        @NotNull Long almacenId,
        @NotNull Long ordenCompraId,
        BigDecimal pesajeKg,
        BigDecimal humedadPct,
        BigDecimal impurezaPct,
        String observaciones,
        @NotEmpty @Valid List<DetalleMovimientoRequest> detalles
) {
}
