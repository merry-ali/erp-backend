package pe.edu.upeu.bomerp.articulo.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.bomerp.articulo.model.UnidadMedida;
import pe.edu.upeu.bomerp.articulo.service.UnidadMedidaService;

import java.util.List;

@org.springframework.security.access.prepost.PreAuthorize("@acceso.permite(authentication, 'INVENTARIO')")
@RestController
@RequestMapping("/api/articulos/unidades-medida")
@RequiredArgsConstructor
public class UnidadMedidaController {

    private final UnidadMedidaService unidadMedidaService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UnidadMedida crear(@Valid @RequestBody UnidadMedida unidadMedida) {
        return unidadMedidaService.crear(unidadMedida);
    }

    @org.springframework.security.access.prepost.PreAuthorize("isAuthenticated()")
    @GetMapping
    public List<UnidadMedida> listar(@RequestParam(required = false, defaultValue = "false") boolean soloActivas) {
        return soloActivas ? unidadMedidaService.listarActivas() : unidadMedidaService.listar();
    }

    @org.springframework.security.access.prepost.PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public UnidadMedida obtener(@PathVariable Long id) {
        return unidadMedidaService.obtener(id);
    }

    @PatchMapping("/{id}/desactivar")
    public UnidadMedida desactivar(@PathVariable Long id) {
        return unidadMedidaService.desactivar(id);
    }
}
