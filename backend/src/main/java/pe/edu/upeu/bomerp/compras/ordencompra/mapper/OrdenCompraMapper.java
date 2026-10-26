package pe.edu.upeu.bomerp.compras.ordencompra.mapper;

import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pe.edu.upeu.bomerp.compras.ordencompra.dto.DetalleOrdenRequest;
import pe.edu.upeu.bomerp.compras.ordencompra.dto.DetalleOrdenResponse;
import pe.edu.upeu.bomerp.compras.ordencompra.dto.OrdenCompraRequest;
import pe.edu.upeu.bomerp.compras.ordencompra.dto.OrdenCompraResponse;
import pe.edu.upeu.bomerp.compras.ordencompra.entity.DetalleOrden;
import pe.edu.upeu.bomerp.compras.ordencompra.entity.OrdenCompra;

import java.util.List;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface OrdenCompraMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fecha", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "total", ignore = true)
    @Mapping(target = "nombreProveedor", ignore = true)
    @Mapping(target = "numeroDocumentoProveedor", ignore = true)
    @Mapping(target = "detalles", ignore = true) // Detalles se mapean a mano en el service
    OrdenCompra toEntity(OrdenCompraRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ordenCompra", ignore = true)
    @Mapping(target = "subtotal", ignore = true) // Se calcula en el service
    @Mapping(target="descripcionArticulo",ignore=true)
    @Mapping(target="precioUnitario",ignore=true)
    DetalleOrden toEntity(DetalleOrdenRequest request);

    OrdenCompraResponse toResponse(OrdenCompra ordenCompra);

    DetalleOrdenResponse toResponse(DetalleOrden detalleOrden);
    
    List<DetalleOrdenResponse> toResponse(List<DetalleOrden> detalles);
}
