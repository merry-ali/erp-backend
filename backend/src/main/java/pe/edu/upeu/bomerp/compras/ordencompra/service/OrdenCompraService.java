package pe.edu.upeu.bomerp.compras.ordencompra.service;
import pe.edu.upeu.bomerp.compras.ordencompra.dto.*;import pe.edu.upeu.bomerp.compras.ordencompra.entity.EstadoOrdenCompra;
import java.util.List;import java.time.LocalDateTime;
public interface OrdenCompraService {
 List<OrdenCompraResponse> listarOrdenes(EstadoOrdenCompra estado,Long proveedorId,LocalDateTime inicio,LocalDateTime fin);
 List<OrdenCompraResponse> listarOrdenes(EstadoOrdenCompra estado,Long proveedorId,LocalDateTime inicio,LocalDateTime fin,String operadorId);
 OrdenCompraResponse obtenerOrden(Long id); OrdenCompraResponse obtenerOrden(Long id,String operadorId);
 OrdenCompraResponse crearOrden(OrdenCompraRequest request); OrdenCompraResponse crearOrden(OrdenCompraRequest request,String operadorId);
 OrdenCompraResponse recibir(Long id); OrdenCompraResponse anular(Long id);
}
