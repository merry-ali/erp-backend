package pe.edu.upeu.bomerp.produccion.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.bomerp.produccion.model.*;
import pe.edu.upeu.bomerp.produccion.repository.OrdenProduccionRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Orquesta el ciclo de vida de una orden de produccion, siguiendo las
 * reglas del documento del proyecto:
 * <ul>
 *   <li>Antes de iniciar, se confirma disponibilidad REAL de insumos con
 *       Inventario (via {@link DisponibilidadInventarioPort}) -- si no
 *       alcanza, la orden queda EN_ESPERA, nunca se descuenta un consumo
 *       que la planta no puede cubrir.</li>
 *   <li>Al ejecutar, se registra el consumo real (puede diferir del
 *       planificado en la formula), el rendimiento, las mermas y los
 *       subproductos.</li>
 *   <li>Solo al cerrar la orden se entregan a Inventario los lotes de
 *       producto terminado resultantes (aqui, via el mismo puerto).</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Transactional
public class OrdenProduccionService {

    private final OrdenProduccionRepository ordenProduccionRepository;
    private final FormulaService formulaService;
    private final pe.edu.upeu.bomerp.inventario.service.StockService stock;
    private final pe.edu.upeu.bomerp.articulo.service.ArticuloService articulos;
    private final DisponibilidadInventarioPort disponibilidadInventarioPort;

    /** Paso 1: planifica la orden a partir de una formula y una cantidad objetivo. */
    public OrdenProduccion planificar(Long formulaId, Long almacenOrigenId, Long almacenDestinoId,
                                       BigDecimal cantidadPlanificada) {
        Formula formula = formulaService.obtener(formulaId);
        if(!formula.isActivo())throw new IllegalArgumentException("Fórmula inactiva");
        stock.validar(almacenOrigenId,formula.getDetalles().getFirst().getCodigoArticulo(),cantidadPlanificada);stock.validar(almacenDestinoId,formula.getCodigoProductoTerminado(),cantidadPlanificada);
        OrdenProduccion orden = new OrdenProduccion();
        orden.setFormula(formula);
        orden.setAlmacenOrigenId(almacenOrigenId);
        orden.setAlmacenDestinoId(almacenDestinoId);
        orden.setCantidadPlanificada(cantidadPlanificada);
        orden.setEstado(OrdenProduccion.EstadoOrden.PLANIFICADA);
        return ordenProduccionRepository.save(orden);
    }

    /**
     * Paso 2: intenta iniciar la orden. Consulta disponibilidad real de
     * cada insumo de la formula (escalado a la cantidad planificada); si
     * alguno no alcanza, la orden pasa a EN_ESPERA en vez de EN_PROCESO.
     */
    public OrdenProduccion iniciar(Long ordenId) {
        OrdenProduccion orden = obtener(ordenId);
        if (orden.getEstado() != OrdenProduccion.EstadoOrden.PLANIFICADA
                && orden.getEstado() != OrdenProduccion.EstadoOrden.EN_ESPERA) {
            throw new IllegalStateException("Solo se puede iniciar una orden PLANIFICADA o EN_ESPERA");
        }

        boolean alcanzaTodo = orden.getFormula().getDetalles().stream().allMatch(detalle -> {
            BigDecimal requerido = detalle.getCantidadPorUnidad().multiply(orden.getCantidadPlanificada());
            BigDecimal disponible = disponibilidadInventarioPort.consultarDisponible(
                    orden.getAlmacenOrigenId(), detalle.getCodigoArticulo());
            return disponible.compareTo(requerido) >= 0;
        });

        if (!alcanzaTodo) {
            orden.setEstado(OrdenProduccion.EstadoOrden.EN_ESPERA);
            return ordenProduccionRepository.save(orden);
        }

        orden.setEstado(OrdenProduccion.EstadoOrden.EN_PROCESO);
        orden.setFechaInicio(LocalDateTime.now());
        return ordenProduccionRepository.save(orden);
    }

    /** Paso 3: registra el consumo real de un insumo durante la ejecucion. */
    public OrdenProduccion registrarConsumo(Long ordenId, String codigoArticulo, Long loteId,
                                             BigDecimal cantidadConsumida, String unidadMedida) {
        OrdenProduccion orden = obtener(ordenId);
        exigirEnProceso(orden);

        if(orden.getFormula().getDetalles().stream().noneMatch(d->d.getCodigoArticulo().equals(codigoArticulo)&&d.getUnidadMedida().equals(unidadMedida)))throw new IllegalArgumentException("El insumo o unidad no pertenece a la fórmula");
        stock.consumir(orden.getAlmacenOrigenId(),codigoArticulo,cantidadConsumida,loteId);
        ConsumoInsumo consumo = new ConsumoInsumo();
        consumo.setCodigoArticulo(codigoArticulo);
        consumo.setLoteId(loteId);
        consumo.setCantidadConsumida(cantidadConsumida);
        consumo.setUnidadMedida(unidadMedida);
        orden.agregarConsumo(consumo);

        return ordenProduccionRepository.save(orden);
    }

    /** Paso 4: registra un resultado (terminado, subproducto, merma, desperdicio o reproceso). */
    public OrdenProduccion registrarResultado(Long ordenId, ProductoResultante.TipoResultado tipo,
                                               String codigoArticulo, BigDecimal cantidad,
                                               String unidadMedida, Long loteGeneradoId) {
        OrdenProduccion orden = obtener(ordenId);
        exigirEnProceso(orden);

        if(cantidad==null||cantidad.signum()<=0)throw new IllegalArgumentException("Cantidad debe ser positiva");
        var a=articulos.obtenerPorCodigo(codigoArticulo);if(!a.getUnidadMedida().getCodigo().equals(unidadMedida))throw new IllegalArgumentException("Unidad incorrecta");
        if(tipo==ProductoResultante.TipoResultado.TERMINADO&&!codigoArticulo.equals(orden.getFormula().getCodigoProductoTerminado()))throw new IllegalArgumentException("Producto diferente al de la fórmula");
        ProductoResultante resultado = new ProductoResultante();
        resultado.setTipo(tipo);
        resultado.setCodigoArticulo(codigoArticulo);
        resultado.setCantidad(cantidad);
        resultado.setUnidadMedida(unidadMedida);
        resultado.setLoteGeneradoId(null);
        orden.agregarResultado(resultado);

        return ordenProduccionRepository.save(orden);
    }

    /**
     * Paso 5: cierra la orden. Calcula la cantidad real total de producto
     * terminado a partir de los resultados registrados. A partir de aqui
     * la orden es la fuente de datos para el calculo de costo de
     * produccion (Costos no tiene de donde partir sin este cierre).
     */
    public OrdenProduccion cerrar(Long ordenId) {
        OrdenProduccion orden = obtener(ordenId);
        exigirEnProceso(orden);

        BigDecimal totalTerminado = orden.getResultados().stream()
                .filter(r -> r.getTipo() == ProductoResultante.TipoResultado.TERMINADO)
                .map(ProductoResultante::getCantidad)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if(totalTerminado.signum()<=0||orden.getConsumos().isEmpty())throw new IllegalStateException("Registra consumos y producto terminado antes de cerrar");
        for(var r:orden.getResultados())if(r.getTipo()==ProductoResultante.TipoResultado.TERMINADO||r.getTipo()==ProductoResultante.TipoResultado.SUBPRODUCTO)r.setLoteGeneradoId(stock.ingresar(orden.getAlmacenDestinoId(),r.getCodigoArticulo(),r.getCantidad(),null));
        orden.setCantidadReal(totalTerminado);
        orden.setEstado(OrdenProduccion.EstadoOrden.CERRADA);
        orden.setFechaFin(LocalDateTime.now());
        return ordenProduccionRepository.save(orden);
    }

    public OrdenProduccion cancelar(Long ordenId) {
        OrdenProduccion orden = obtener(ordenId);
        if (!orden.getConsumos().isEmpty() || orden.getEstado() == OrdenProduccion.EstadoOrden.CERRADA) {
            throw new IllegalStateException("No se puede cancelar una orden ya CERRADA");
        }
        orden.setEstado(OrdenProduccion.EstadoOrden.CANCELADA);
        return ordenProduccionRepository.save(orden);
    }

    @Transactional(readOnly = true)
    public OrdenProduccion obtener(Long id) {
        return ordenProduccionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Orden de produccion no encontrada: " + id));
    }

    @Transactional(readOnly = true)
    public List<OrdenProduccion> listar() {
        return ordenProduccionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<OrdenProduccion> listarPorEstado(OrdenProduccion.EstadoOrden estado) {
        return ordenProduccionRepository.findByEstado(estado);
    }

    private void exigirEnProceso(OrdenProduccion orden) {
        if (orden.getEstado() != OrdenProduccion.EstadoOrden.EN_PROCESO) {
            throw new IllegalStateException("La orden debe estar EN_PROCESO (actual: " + orden.getEstado() + ")");
        }
    }
}
