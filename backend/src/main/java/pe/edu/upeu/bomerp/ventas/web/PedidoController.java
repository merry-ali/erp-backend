package pe.edu.upeu.bomerp.ventas.web;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import java.util.*;
import pe.edu.upeu.bomerp.ventas.entity.Pedido;
import pe.edu.upeu.bomerp.ventas.service.PedidoService;
import pe.edu.upeu.bomerp.ventas.web.dto.PedidoRequest;
import pe.edu.upeu.bomerp.ventas.web.dto.MotivoRequest;


@RestController 
@RequestMapping("/api/ventas/pedidos") 
@RequiredArgsConstructor 
@PreAuthorize("@acceso.permite(authentication,'VENTAS')") 
public class PedidoController {
    private final PedidoService service;

    @GetMapping 
    @PreAuthorize("@acceso.permite(authentication,'VENTAS') or @acceso.permite(authentication,'INVENTARIO')") 
    public List<Pedido> listar(){
        return service.listar();
    }

    @PostMapping 
    public Pedido crear(@Valid @RequestBody PedidoRequest r,Authentication a)
    {
        return service.crear(r,a.getName());
    }

    @PostMapping("/venta-rapida")
    public Pedido ventaRapida(@Valid @RequestBody PedidoRequest r, Authentication a) {
        return service.ventaRapida(r, a.getName());
    }
    @PostMapping("/{id}/aprobar") 
    public Pedido aprobar(@PathVariable Long id)
    {
        return service.aprobar(id);
    }
    @PostMapping("/{id}/entregar") 
    @PreAuthorize("@acceso.permite(authentication,'INVENTARIO')") 
    public Pedido entregar(@PathVariable Long id,@RequestBody Map<String,String> r){
        return service.entregar(id,r.get("vehiculo"));
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
