package pe.edu.upeu.bomerp.compras.ordencompra.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record OrdenCompraRequest(
        @NotNull(message = "El proveedor es obligatorio")
        Long proveedorId,

        @Size(max = 255, message = "Las condiciones pactadas no pueden exceder 255 caracteres")
        String condicionesPactadas,

        @NotEmpty(message = "Debe haber al menos un detalle en la orden de compra")
        @Valid
        List<DetalleOrdenRequest> detalles
) {
}
