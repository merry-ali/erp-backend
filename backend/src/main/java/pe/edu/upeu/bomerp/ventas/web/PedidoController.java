package pe.edu.upeu.bomerp.ventas.web;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import java.util.*;
import java.math.BigDecimal;
import pe.edu.upeu.bomerp.ventas.entity.Pedido;
import pe.edu.upeu.bomerp.ventas.service.PedidoService;
import pe.edu.upeu.bomerp.ventas.web.dto.PedidoRequest;
import pe.edu.upeu.bomerp.ventas.web.dto.MotivoRequest;
import pe.edu.upeu.bomerp.inventario.service.InventarioService;


@RestController 
@RequestMapping("/api/ventas/pedidos") 
@RequiredArgsConstructor 
@PreAuthorize("@acceso.permite(authentication,'VENTAS')") 
public class PedidoController {
    private final PedidoService service;
    private final InventarioService inventario;

    @GetMapping 
    @PreAuthorize("@acceso.permite(authentication,'VENTAS') or @acceso.permite(authentication,'INVENTARIO')") 
    public List<Pedido> listar(){
        return service.listar();
    }

    @GetMapping("/{id}/proceso")
    public List<pe.edu.upeu.bomerp.inventario.model.MovimientoInventario> proceso(@PathVariable Long id) {
        return inventario.listarMovimientosPedido(id);
    }

    @PostMapping 
    public Pedido crear(@Valid @RequestBody PedidoRequest r,Authentication a)
    {
        return service.crear(r,a.getName());
    }

    @PutMapping("/{id}")
    public Pedido actualizarBorrador(@PathVariable Long id, @Valid @RequestBody PedidoRequest r, Authentication a) {
        return service.actualizarBorrador(id, r, a.getName());
    }

    @PostMapping("/venta-rapida")
    public Pedido ventaRapida(@Valid @RequestBody PedidoRequest r, Authentication a) {
        String nombreEmisor = a.getName();
        if (a instanceof JwtAuthenticationToken jwt) {
            for (String claim : List.of("name", "preferred_username", "email")) {
                String valor = jwt.getToken().getClaimAsString(claim);
                if (valor != null && !valor.isBlank()) {
                    nombreEmisor = valor;
                    break;
                }
            }
        }
        return service.ventaRapida(r, a.getName(), nombreEmisor);
    }
    @PostMapping("/{id}/aprobar") 
    public Pedido aprobar(@PathVariable Long id)
    {
        return service.aprobar(id);
    }
    @PostMapping("/{id}/entregar") 
    @PreAuthorize("@acceso.permite(authentication,'INVENTARIO')") 
    public Pedido entregar(@PathVariable Long id,@RequestBody Map<String,String> r){
        String tipoEntrega = r.get("tipoEntrega");
        if (tipoEntrega == null) tipoEntrega = r.get("vehiculo") == null || r.get("vehiculo").isBlank() ? "LOCAL" : "VEHICULO";
        return service.entregar(id, tipoEntrega, r.get("vehiculo"));
    }

    @PostMapping("/{id}/pagos")
    public Pedido registrarPago(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        Object monto = request.get("monto");
        BigDecimal importe;
        try {
            importe = new BigDecimal(String.valueOf(monto));
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Ingresa un monto de pago válido");
        }
        Object formaPago = request.get("formaPago");
        Object referencia = request.get("referencia");
        return service.registrarPago(id, importe, formaPago == null ? null : String.valueOf(formaPago),
                referencia == null ? null : String.valueOf(referencia));
    }
    
    @PostMapping("/{id}/anular") @PreAuthorize("hasRole('ADMIN')")
    public Pedido anular(@PathVariable Long id, @Valid @RequestBody MotivoRequest request){
        return service.anular(id, request.motivo());
    }

    @PostMapping("/{id}/revertir") @PreAuthorize("hasRole('ADMIN')")
    public Pedido revertir(@PathVariable Long id, @Valid @RequestBody MotivoRequest request) {
        return service.revertir(id, request.motivo());
    }
}
