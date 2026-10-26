package pe.edu.upeu.bomerp.produccion.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.bomerp.produccion.model.Formula;
import pe.edu.upeu.bomerp.produccion.service.FormulaService;

import java.util.List;

@org.springframework.security.access.prepost.PreAuthorize("@acceso.permite(authentication, 'PRODUCCION')")
@RestController
@RequestMapping("/api/produccion/formulas")
@RequiredArgsConstructor
public class FormulaController {

    private final FormulaService formulaService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Formula crear(@Valid @RequestBody Formula formula) {
        return formulaService.crear(formula);
    }

    @GetMapping
    public List<Formula> listar() {
        return formulaService.listar();
    }

    @GetMapping("/{id}")
    public Formula obtener(@PathVariable Long id) {
        return formulaService.obtener(id);
    }

    @PatchMapping("/{id}/desactivar")
    public Formula desactivar(@PathVariable Long id) {
        return formulaService.desactivar(id);
    }
}
