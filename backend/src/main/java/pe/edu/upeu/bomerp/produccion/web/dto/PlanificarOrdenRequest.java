package pe.edu.upeu.bomerp.produccion.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PlanificarOrdenRequest(
        @NotNull Long formulaId,
        @NotNull Long almacenOrigenId,
        @NotNull Long almacenDestinoId,
        @NotNull @Positive BigDecimal cantidadPlanificada
) {
}
