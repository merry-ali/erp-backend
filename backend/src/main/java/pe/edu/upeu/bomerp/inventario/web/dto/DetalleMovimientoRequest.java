package pe.edu.upeu.bomerp.inventario.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * {@code loteId} es obligatorio solo para detalles de despacho
 * (seleccion de lote); en recepcion se ignora, el lote se genera al
 * confirmar.
 */
public record DetalleMovimientoRequest(
        @NotBlank String codigoArticulo,
        Long loteId,
        @NotNull @Positive BigDecimal cantidad,
        @NotBlank String unidadMedida
) {
}
