package pe.edu.upeu.bomerp.compras.ordencompra.event;

import pe.edu.upeu.bomerp.compras.ordencompra.dto.DetalleOrdenResponse;

import java.math.BigDecimal;
import java.util.List;

/**
 * Evento de dominio emitido cuando una Orden de Compra es creada exitosamente.
 * Este evento está diseñado para ser escuchado por otros módulos (ej. Inventario)
 * a través de Spring Modulith, permitiendo el desacoplamiento.
 */
public record OrdenCompraEmitidaEvent(
        Long ordenId,
        Long proveedorId,
        BigDecimal total,
        List<DetalleOrdenResponse> detalles
) {
}
