package pe.edu.upeu.bomerp.administracion.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.bomerp.administracion.model.Sucursal;
import pe.edu.upeu.bomerp.administracion.service.SucursalService;

import java.util.List;

@org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/api/administracion/empresas/{empresaId}/sucursales")
@RequiredArgsConstructor
public class SucursalController {

    private final SucursalService sucursalService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Sucursal crear(@PathVariable Long empresaId, @Valid @RequestBody Sucursal sucursal) {
        return sucursalService.crear(empresaId, sucursal);
    }

    @GetMapping
    public List<Sucursal> listar(@PathVariable Long empresaId) {
        return sucursalService.listarPorEmpresa(empresaId);
    }
}
