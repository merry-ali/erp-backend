package pe.edu.upeu.bomerp.inventario.web.dto;

import jakarta.validation.constraints.NotNull;

public record ConfirmarRecepcionRequest(
        @NotNull Boolean aceptado
) {
}
