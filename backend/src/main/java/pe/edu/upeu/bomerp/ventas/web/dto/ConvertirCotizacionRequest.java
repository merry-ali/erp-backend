package pe.edu.upeu.bomerp.ventas.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ConvertirCotizacionRequest(@NotNull @Positive Long almacenId) {
}
