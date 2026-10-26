package pe.edu.upeu.bomerp.inventario.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.bomerp.inventario.model.DetalleMovimiento;
import pe.edu.upeu.bomerp.inventario.model.Lote;
import pe.edu.upeu.bomerp.inventario.model.MovimientoInventario;
import pe.edu.upeu.bomerp.inventario.service.InventarioService;
import pe.edu.upeu.bomerp.inventario.web.dto.*;

import java.util.List;

@org.springframework.security.access.prepost.PreAuthorize("@acceso.permite(authentication, 'INVENTARIO')")
@RestController
@RequestMapping("/api/inventario")
@RequiredArgsConstructor
public class InventarioController {

    private final InventarioService inventarioService;

    // -------- Recepcion --------

    @PostMapping("/recepciones")
    @ResponseStatus(HttpStatus.CREATED)
    public MovimientoInventario registrarRecepcion(@Valid @RequestBody RecepcionRequest request) {
        List<DetalleMovimiento> detalles = request.detalles().stream()
                .map(this::aDetalle)
                .toList();
        return inventarioService.registrarRecepcion(
                request.almacenId(), request.ordenCompraId(),
                request.pesajeKg(), request.humedadPct(), request.impurezaPct(),
                request.observaciones(), detalles);
    }

    @PostMapping("/recepciones/{id}/confirmar")
    public MovimientoInventario confirmarRecepcion(@PathVariable Long id,
                                                     @Valid @RequestBody ConfirmarRecepcionRequest request) {
        return inventarioService.confirmarRecepcion(id, request.aceptado());
    }

    // -------- Despacho --------

    @org.springframework.security.access.prepost.PreAuthorize("denyAll()")
    @PostMapping("/despachos")
    @ResponseStatus(HttpStatus.CREATED)
    public MovimientoInventario registrarDespacho(@Valid @RequestBody DespachoRequest request) {
        List<DetalleMovimiento> detalles = request.detalles().stream()
                .map(this::aDetalle)
                .toList();
        return inventarioService.registrarDespacho(
                request.almacenId(), request.pedidoVentaId(),
                request.vehiculoPlaca(), request.observaciones(), detalles);
    }

    @org.springframework.security.access.prepost.PreAuthorize("denyAll()")
    @PostMapping("/despachos/{id}/confirmar-entrega")
    public MovimientoInventario confirmarEntrega(@PathVariable Long id) {
        return inventarioService.confirmarEntrega(id);
    }

    // -------- Comunes --------

    @PostMapping("/movimientos/{id}/anular")
    public MovimientoInventario anular(@PathVariable Long id) {
        return inventarioService.anular(id);
    }

    @GetMapping("/movimientos")
    public List<MovimientoInventario> listar(@RequestParam(required = false) MovimientoInventario.TipoMovimiento tipo) {
        return inventarioService.listar(tipo);
    }

    @GetMapping("/movimientos/{id}")
    public MovimientoInventario obtener(@PathVariable Long id) {
        return inventarioService.obtener(id);
    }

    // -------- Consultas de stock (usadas por Ventas y Produccion) --------

    @GetMapping("/stock")
    @org.springframework.security.access.prepost.PreAuthorize("@acceso.permite(authentication, 'INVENTARIO') or @acceso.permite(authentication, 'VENTAS')")
    public StockResponse consultarStock(@RequestParam Long almacenId, @RequestParam String codigoArticulo) {
        return new StockResponse(almacenId, codigoArticulo,
                inventarioService.consultarDisponible(almacenId, codigoArticulo));
    }

    @GetMapping("/lotes")
    public List<Lote> listarLotesDisponibles(@RequestParam Long almacenId, @RequestParam String codigoArticulo) {
        return inventarioService.listarLotesDisponibles(almacenId, codigoArticulo);
    }

    private DetalleMovimiento aDetalle(DetalleMovimientoRequest r) {
        DetalleMovimiento detalle = new DetalleMovimiento();
        detalle.setCodigoArticulo(r.codigoArticulo());
        detalle.setLoteId(r.loteId());
        detalle.setCantidad(r.cantidad());
        detalle.setUnidadMedida(r.unidadMedida());
        return detalle;
    }
}
