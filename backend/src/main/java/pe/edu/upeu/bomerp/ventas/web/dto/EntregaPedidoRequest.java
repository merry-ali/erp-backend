package pe.edu.upeu.bomerp.ventas.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

/** Lotes y cantidades que Almacén confirma para entregar un pedido aprobado. */
public record EntregaPedidoRequest(
        @NotBlank @Size(max = 15) String tipoEntrega,
        @Size(max = 15) String vehiculo,
        @NotEmpty List<@NotNull @Valid Linea> detalles) {

    public record Linea(
            @NotBlank @Size(max = 30) String codigoArticulo,
            @NotNull @Positive Long loteId,
            @NotNull @Positive @Digits(integer = 8, fraction = 4) BigDecimal cantidad) {
    }
}
