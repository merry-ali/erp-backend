package pe.edu.upeu.bomerp.administracion.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.bomerp.administracion.model.Tienda;
import pe.edu.upeu.bomerp.administracion.service.TiendaService;

import java.util.List;

@org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/api/administracion/sucursales/{sucursalId}/tiendas")
@RequiredArgsConstructor
public class TiendaController {

    private final TiendaService tiendaService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Tienda crear(@PathVariable Long sucursalId, @Valid @RequestBody Tienda tienda) {
        return tiendaService.crear(sucursalId, tienda);
    }

    @GetMapping
    public List<Tienda> listar(@PathVariable Long sucursalId) {
        return tiendaService.listarPorSucursal(sucursalId);
    }
}
