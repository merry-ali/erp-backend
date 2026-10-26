package pe.edu.upeu.bomerp.compras.ordencompra.dto;
import jakarta.validation.constraints.*;
public record DetalleOrdenRequest(@NotNull @Positive Long articuloId,@NotNull @Min(1) @Max(1000000) Integer cantidad) {}
