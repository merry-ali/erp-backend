package pe.edu.upeu.bomerp.administracion.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.bomerp.administracion.model.Almacen;
import pe.edu.upeu.bomerp.administracion.service.AlmacenService;

import java.util.List;

@org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/api/administracion/sucursales/{sucursalId}/almacenes")
@RequiredArgsConstructor
public class AlmacenController {

    private final AlmacenService almacenService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Almacen crear(@PathVariable Long sucursalId, @Valid @RequestBody Almacen almacen) {
        return almacenService.crear(sucursalId, almacen);
    }

    @GetMapping
    public List<Almacen> listar(@PathVariable Long sucursalId) {
        return almacenService.listarPorSucursal(sucursalId);
    }
}
