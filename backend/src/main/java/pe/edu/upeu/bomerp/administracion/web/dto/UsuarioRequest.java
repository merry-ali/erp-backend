package pe.edu.upeu.bomerp.administracion.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UsuarioRequest(
        @NotNull Long personaId,
        @NotNull Long rolId,
        @NotBlank String username,
        String keycloakSub
) {
}
