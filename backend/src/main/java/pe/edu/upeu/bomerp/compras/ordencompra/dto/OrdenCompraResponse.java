package pe.edu.upeu.bomerp.compras.ordencompra.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.edu.upeu.bomerp.compras.ordencompra.entity.EstadoOrdenCompra;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdenCompraResponse {
    private Long id;
    private LocalDateTime fecha;
    private EstadoOrdenCompra estado;
    private BigDecimal total;
    private Long proveedorId;
    private String nombreProveedor;
    private String numeroDocumentoProveedor;
    private String condicionesPactadas;
    private List<DetalleOrdenResponse> detalles;
}
