package pe.edu.upeu.bomerp.ventas.web;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import pe.edu.upeu.bomerp.ventas.entity.CotizacionVenta;
import pe.edu.upeu.bomerp.ventas.entity.Pedido;
import pe.edu.upeu.bomerp.ventas.service.CotizacionVentaService;
import pe.edu.upeu.bomerp.ventas.web.dto.ConvertirCotizacionRequest;
import pe.edu.upeu.bomerp.ventas.web.dto.CrearCotizacionRequest;

@RestController
@RequestMapping("/api/ventas/cotizaciones")
@RequiredArgsConstructor
@PreAuthorize("@acceso.permite(authentication,'VENTAS')")
public class CotizacionVentaController {

    private final CotizacionVentaService service;

    @GetMapping
    public List<CotizacionVenta> listar() {
        return service.listar();
    }

    @PostMapping
    public CotizacionVenta crear(@Valid @RequestBody CrearCotizacionRequest request) {
        return service.crear(request);
    }

    @PostMapping("/{id}/enviar")
    public CotizacionVenta enviar(@PathVariable Long id) {
        return service.enviar(id);
    }

    @PostMapping("/{id}/aceptar")
    public CotizacionVenta aceptar(@PathVariable Long id) {
        return service.aceptar(id);
    }

    @PostMapping("/{id}/rechazar")
    public CotizacionVenta rechazar(@PathVariable Long id) {
        return service.rechazar(id);
    }

    @PostMapping("/{id}/pedido")
    public Pedido convertirEnPedido(
            @PathVariable Long id,
            @Valid @RequestBody ConvertirCotizacionRequest request,
            Authentication authentication) {
        return service.convertirEnPedido(id, request, authentication.getName());
    }
}
