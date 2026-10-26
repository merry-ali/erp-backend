package pe.edu.upeu.bomerp.administracion.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.bomerp.administracion.model.Empresa;
import pe.edu.upeu.bomerp.administracion.service.EmpresaService;

import java.util.List;

@org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/api/administracion/empresas")
@RequiredArgsConstructor
public class EmpresaController {

    private final EmpresaService empresaService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Empresa crear(@Valid @RequestBody Empresa empresa) {
        return empresaService.crear(empresa);
    }

    @GetMapping
    public List<Empresa> listar() {
        return empresaService.listar();
    }

    @GetMapping("/{id}")
    public Empresa obtener(@PathVariable Long id) {
        return empresaService.obtener(id);
    }

    @PutMapping("/{id}")
    public Empresa actualizar(@PathVariable Long id, @Valid @RequestBody Empresa cambios) {
        return empresaService.actualizar(id, cambios);
    }
}
