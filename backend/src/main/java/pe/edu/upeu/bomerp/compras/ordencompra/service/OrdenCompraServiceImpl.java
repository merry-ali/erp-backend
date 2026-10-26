package pe.edu.upeu.bomerp.compras.ordencompra.service;
import lombok.RequiredArgsConstructor;import org.springframework.stereotype.Service;import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationEventPublisher;import org.springframework.security.access.AccessDeniedException;
import pe.edu.upeu.bomerp.compras.ordencompra.dto.*;import pe.edu.upeu.bomerp.compras.ordencompra.entity.*;
import pe.edu.upeu.bomerp.compras.ordencompra.event.OrdenCompraEmitidaEvent;
import pe.edu.upeu.bomerp.compras.ordencompra.mapper.OrdenCompraMapper;import pe.edu.upeu.bomerp.compras.ordencompra.repository.OrdenCompraRepository;
import pe.edu.upeu.bomerp.personas.persona.service.PersonaService;import pe.edu.upeu.bomerp.articulo.service.ArticuloService;
import pe.edu.upeu.bomerp.exception.*;import java.time.LocalDateTime;import java.math.*;import java.util.*;
@Service @RequiredArgsConstructor @Transactional
public class OrdenCompraServiceImpl implements OrdenCompraService {
 private final OrdenCompraRepository repo; private final OrdenCompraMapper mapper; private final PersonaService personas;
 private final ArticuloService articulos; private final ApplicationEventPublisher eventos;
 public OrdenCompraResponse crearOrden(OrdenCompraRequest r){return crearOrden(r,null);}
 public OrdenCompraResponse crearOrden(OrdenCompraRequest r,String operadorId){
  var proveedor=personas.obtenerProveedorActivo(r.proveedorId());var orden=new OrdenCompra();
  orden.setFecha(LocalDateTime.now());orden.setEstado(EstadoOrdenCompra.EMITIDA);orden.setOperadorId(operadorId);
  orden.setProveedorId(proveedor.getId());orden.setNombreProveedor(proveedor.getNombreORazonSocial());orden.setNumeroDocumentoProveedor(proveedor.getNumeroDocumento());orden.setCondicionesPactadas(r.condicionesPactadas());
  BigDecimal total=BigDecimal.ZERO;
  for(var linea:r.detalles()){
   var articulo=articulos.obtener(linea.articuloId());if(!articulo.isActivo())throw new EstadoOrdenException("El artículo está inactivo");
   var detalle=new DetalleOrden();detalle.setArticuloId(articulo.getId());detalle.setDescripcionArticulo(articulo.getNombre());detalle.setPrecioUnitario(articulo.getCostoReferencial());detalle.setCantidad(linea.cantidad());
   detalle.setSubtotal(articulo.getCostoReferencial().multiply(BigDecimal.valueOf(linea.cantidad())).setScale(2,RoundingMode.HALF_UP));
   orden.addDetalle(detalle);total=total.add(detalle.getSubtotal());
  }
  if(total.precision()-total.scale()>10)throw new IllegalArgumentException("Total fuera de rango");
  orden.setTotal(total);var response=dto(repo.save(orden));eventos.publishEvent(new OrdenCompraEmitidaEvent(response.getId(),response.getProveedorId(),response.getTotal(),response.getDetalles()));return response;
 }
 @Transactional(readOnly=true) public List<OrdenCompraResponse> listarOrdenes(EstadoOrdenCompra e,Long p,LocalDateTime i,LocalDateTime f){return listarOrdenes(e,p,i,f,null);}
 @Transactional(readOnly=true) public List<OrdenCompraResponse> listarOrdenes(EstadoOrdenCompra e,Long p,LocalDateTime i,LocalDateTime f,String owner){if(i!=null&&f!=null&&i.isAfter(f))throw new IllegalArgumentException();return repo.buscar(e,p,i,f,owner).stream().map(this::dto).toList();}
 @Transactional(readOnly=true) public OrdenCompraResponse obtenerOrden(Long id){return obtenerOrden(id,null);}
 @Transactional(readOnly=true) public OrdenCompraResponse obtenerOrden(Long id,String owner){var o=repo.findWithDetallesById(id).orElseThrow(()->new ResourceNotFoundException("Orden no encontrada: "+id));if(owner!=null&&!owner.equals(o.getOperadorId()))throw new AccessDeniedException("La orden pertenece a otro operador");return dto(o);}
 public OrdenCompraResponse recibir(Long id){var o=bloquearEmitida(id);Map<Long,Integer> cantidades=new TreeMap<>();o.getDetalles().forEach(d->cantidades.merge(d.getArticuloId(),d.getCantidad(),Math::addExact));throw new IllegalStateException("Recibe la mercancía desde Inventario con pesaje y control de calidad");/*o.setEstado(EstadoOrdenCompra.RECIBIDA);return dto(o);*/}
 public OrdenCompraResponse anular(Long id){var o=bloquearEmitida(id);o.setEstado(EstadoOrdenCompra.ANULADA);return dto(o);}
 private OrdenCompra bloquearEmitida(Long id){var o=repo.bloquear(id).orElseThrow(()->new ResourceNotFoundException("Orden no encontrada: "+id));if(o.getEstado()!=EstadoOrdenCompra.EMITIDA)throw new EstadoOrdenException("Solo una orden EMITIDA puede recibirse o anularse");return o;}
 private OrdenCompraResponse dto(OrdenCompra o){var d=mapper.toResponse(o);d.setDetalles(mapper.toResponse(o.getDetalles()));return d;}
}
