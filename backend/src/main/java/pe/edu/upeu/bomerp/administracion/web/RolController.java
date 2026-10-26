package pe.edu.upeu.bomerp.administracion.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.bomerp.administracion.model.Rol;
import pe.edu.upeu.bomerp.administracion.service.RolService;
import pe.edu.upeu.bomerp.administracion.web.dto.AsignarPermisosRequest;

import java.util.List;

@org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/api/administracion/roles")
@RequiredArgsConstructor
public class RolController {

    private final RolService rolService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Rol crear(@Valid @RequestBody Rol rol) {
        return rolService.crear(rol);
    }

    @GetMapping
    public List<Rol> listar() {
        return rolService.listar();
    }

    @PutMapping("/{id}/permisos")
    public Rol asignarPermisos(@PathVariable Long id, @RequestBody AsignarPermisosRequest request) {
        return rolService.asignarPermisos(id, request.permisoIds());
    }
}
