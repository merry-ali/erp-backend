package pe.edu.upeu.bomerp.ventas.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MotivoRequest(@NotBlank @Size(max = 300) String motivo) {}
