package pe.edu.upeu.bomerp.ventas.web.dto;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CrearCotizacionRequest(
        @NotNull @Positive Long clienteId,
        @jakarta.validation.constraints.Size(max = 30) String formaPago,
        @jakarta.validation.constraints.Size(max = 30) String plazoPago,
        @NotEmpty List<@Valid Linea> detalles) {

    public record Linea(
            @NotBlank String codigoArticulo,
            @NotNull @Positive BigDecimal cantidad,
            @NotNull @DecimalMin("0") BigDecimal precioUnitario,
            @NotNull @DecimalMin("0") @DecimalMax("100") BigDecimal descuento) {
    }
}
