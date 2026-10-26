package pe.edu.upeu.bomerp.inventario.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record DespachoRequest(
        @NotNull Long almacenId,
        @NotNull Long pedidoVentaId,
        @NotBlank String vehiculoPlaca,
        String observaciones,
        @NotEmpty @Valid List<DetalleMovimientoRequest> detalles
) {
}
