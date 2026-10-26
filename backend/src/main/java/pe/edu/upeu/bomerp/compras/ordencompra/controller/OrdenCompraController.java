package pe.edu.upeu.bomerp.compras.ordencompra.controller;
import lombok.RequiredArgsConstructor;import org.springframework.web.bind.annotation.*;import org.springframework.http.*;import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;import org.springframework.security.oauth2.jwt.Jwt;import jakarta.validation.Valid;
import pe.edu.upeu.bomerp.compras.ordencompra.dto.*;import pe.edu.upeu.bomerp.compras.ordencompra.entity.EstadoOrdenCompra;import pe.edu.upeu.bomerp.compras.ordencompra.service.OrdenCompraService;
import java.time.LocalDateTime;import java.util.*;import java.math.BigDecimal;
@org.springframework.security.access.prepost.PreAuthorize("@acceso.permite(authentication, 'COMPRAS')")
@RestController @RequestMapping("/api/v1/ordenes-compra") @RequiredArgsConstructor
public class OrdenCompraController {
 private final OrdenCompraService service;
 private String owner(Authentication a){return a!=null&&a.getAuthorities().stream().anyMatch(r->r.getAuthority().equals("ROLE_OPERADOR"))&&a.getAuthorities().stream().noneMatch(r->Set.of("ROLE_SUPERVISOR","ROLE_ADMIN").contains(r.getAuthority()))?a.getName():null;}
 @org.springframework.security.access.prepost.PreAuthorize("@acceso.permite(authentication,'COMPRAS') or @acceso.permite(authentication,'INVENTARIO')") @GetMapping public ResponseEntity<List<OrdenCompraResponse>> listarOrdenes(@RequestParam(required=false)EstadoOrdenCompra estado,@RequestParam(required=false)Long proveedorId,
  @RequestParam(required=false)@DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME)LocalDateTime inicio,@RequestParam(required=false)@DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME)LocalDateTime fin,Authentication a){return ResponseEntity.ok(service.listarOrdenes(estado,proveedorId,inicio,fin,owner(a)));}
 @org.springframework.security.access.prepost.PreAuthorize("@acceso.permite(authentication,'COMPRAS') or @acceso.permite(authentication,'INVENTARIO')") @GetMapping("/{id}") public ResponseEntity<OrdenCompraResponse> obtenerOrden(@PathVariable Long id,Authentication a){return ResponseEntity.ok(a==null?service.obtenerOrden(id):service.obtenerOrden(id,owner(a)));}
 @PostMapping public ResponseEntity<OrdenCompraResponse> crearOrden(@Valid @RequestBody OrdenCompraRequest r,Authentication a){return ResponseEntity.status(201).body(a!=null&&a.getPrincipal() instanceof Jwt jwt?service.crearOrden(r,jwt.getSubject()):service.crearOrden(r));}
 @PatchMapping("/{id}/recibir")public OrdenCompraResponse recibir(@PathVariable Long id){return service.recibir(id);}
 @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')") @PatchMapping("/{id}/anular")public OrdenCompraResponse anular(@PathVariable Long id){return service.anular(id);}
 @org.springframework.security.access.prepost.PreAuthorize("@acceso.permite(authentication,'COMPRAS') or @acceso.permite(authentication,'INVENTARIO')") @GetMapping("/resumen")public Map<String,Object> resumen(@RequestParam(required=false)EstadoOrdenCompra estado,@RequestParam(required=false)Long proveedorId,
  @RequestParam(required=false)@DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME)LocalDateTime inicio,@RequestParam(required=false)@DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME)LocalDateTime fin){
  var lista=service.listarOrdenes(estado,proveedorId,inicio,fin);var monto=lista.stream().map(OrdenCompraResponse::getTotal).reduce(BigDecimal.ZERO,BigDecimal::add);
  return Map.of("cantidad",lista.size(),"montoTotal",monto,"promedio",lista.isEmpty()?BigDecimal.ZERO:monto.divide(BigDecimal.valueOf(lista.size()),2,java.math.RoundingMode.HALF_UP));}
}
