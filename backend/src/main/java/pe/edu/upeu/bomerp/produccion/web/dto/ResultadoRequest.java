package pe.edu.upeu.bomerp.produccion.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import pe.edu.upeu.bomerp.produccion.model.ProductoResultante;

import java.math.BigDecimal;

public record ResultadoRequest(
        @NotNull ProductoResultante.TipoResultado tipo,
        @NotBlank String codigoArticulo,
        @NotNull BigDecimal cantidad,
        @NotBlank String unidadMedida,
        Long loteGeneradoId
) {
}
