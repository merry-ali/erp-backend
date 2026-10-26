package pe.edu.upeu.bomerp.produccion.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.bomerp.produccion.model.OrdenProduccion;
import pe.edu.upeu.bomerp.produccion.service.OrdenProduccionService;
import pe.edu.upeu.bomerp.produccion.web.dto.ConsumoRequest;
import pe.edu.upeu.bomerp.produccion.web.dto.PlanificarOrdenRequest;
import pe.edu.upeu.bomerp.produccion.web.dto.ResultadoRequest;

import java.util.List;

@org.springframework.security.access.prepost.PreAuthorize("@acceso.permite(authentication, 'PRODUCCION')")
@RestController
@RequestMapping("/api/produccion/ordenes")
@RequiredArgsConstructor
public class OrdenProduccionController {

    private final OrdenProduccionService ordenProduccionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrdenProduccion planificar(@Valid @RequestBody PlanificarOrdenRequest request) {
        return ordenProduccionService.planificar(
                request.formulaId(), request.almacenOrigenId(),
                request.almacenDestinoId(), request.cantidadPlanificada());
    }

    @GetMapping
    public List<OrdenProduccion> listar(@RequestParam(required = false) OrdenProduccion.EstadoOrden estado) {
        return estado == null ? ordenProduccionService.listar() : ordenProduccionService.listarPorEstado(estado);
    }

    @GetMapping("/{id}")
    public OrdenProduccion obtener(@PathVariable Long id) {
        return ordenProduccionService.obtener(id);
    }

    @PostMapping("/{id}/iniciar")
    public OrdenProduccion iniciar(@PathVariable Long id) {
        return ordenProduccionService.iniciar(id);
    }

    @PostMapping("/{id}/consumos")
    public OrdenProduccion registrarConsumo(@PathVariable Long id, @Valid @RequestBody ConsumoRequest request) {
        return ordenProduccionService.registrarConsumo(
                id, request.codigoArticulo(), request.loteId(),
                request.cantidadConsumida(), request.unidadMedida());
    }

    @PostMapping("/{id}/resultados")
    public OrdenProduccion registrarResultado(@PathVariable Long id, @Valid @RequestBody ResultadoRequest request) {
        return ordenProduccionService.registrarResultado(
                id, request.tipo(), request.codigoArticulo(),
                request.cantidad(), request.unidadMedida(), request.loteGeneradoId());
    }

    @PostMapping("/{id}/cerrar")
    public OrdenProduccion cerrar(@PathVariable Long id) {
        return ordenProduccionService.cerrar(id);
    }

    @PostMapping("/{id}/cancelar")
    public OrdenProduccion cancelar(@PathVariable Long id) {
        return ordenProduccionService.cancelar(id);
    }
}
