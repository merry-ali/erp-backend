package pe.edu.upeu.bomerp.produccion.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ConsumoRequest(
        @NotBlank String codigoArticulo,
        Long loteId,
        @NotNull @Positive BigDecimal cantidadConsumida,
        @NotBlank String unidadMedida
) {
}
