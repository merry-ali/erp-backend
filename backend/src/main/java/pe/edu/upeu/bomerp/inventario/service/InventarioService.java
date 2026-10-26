package pe.edu.upeu.bomerp.inventario.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.bomerp.inventario.model.DetalleMovimiento;
import pe.edu.upeu.bomerp.inventario.model.Lote;
import pe.edu.upeu.bomerp.inventario.model.MovimientoInventario;
import pe.edu.upeu.bomerp.inventario.repository.LoteRepository;
import pe.edu.upeu.bomerp.inventario.repository.MovimientoInventarioRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Ejecuta y refleja los movimientos fisicos del almacen: recepcion
 * (compras), despacho (ventas) y la consulta de existencias reales que
 * Ventas y Produccion necesitan antes de comprometerse. Este modulo NO
 * decide si un pedido o una orden de compra son validos -- solo ejecuta
 * el movimiento fisico y responde con la verdad de lo que hay en el
 * almacen.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class InventarioService {

    private final MovimientoInventarioRepository movimientoRepository;
    private final LoteRepository loteRepository;
    private final StockService stock;
    private final pe.edu.upeu.bomerp.compras.ordencompra.repository.OrdenCompraRepository compras;
    private final pe.edu.upeu.bomerp.articulo.service.ArticuloService articulos;

    // ---------------------------------------------------------------
    // Recepcion (entrada, ligada a una orden de compra)
    // ---------------------------------------------------------------

    /**
     * Paso 1: registra la recepcion fisica de mercancia (pesaje, humedad,
     * impurezas) en BORRADOR, vinculada a la orden de compra. Todavia no
     * genera lotes -- eso solo ocurre si se confirma como aceptada.
     */
    public MovimientoInventario registrarRecepcion(Long almacenId, Long ordenCompraId,
                                                     BigDecimal pesajeKg, BigDecimal humedadPct,
                                                     BigDecimal impurezaPct, String observaciones,
                                                     List<DetalleMovimiento> detalles) {
        var compra=compras.bloquear(ordenCompraId).orElseThrow(()->new IllegalArgumentException("Orden de compra inexistente"));
        if(compra.getEstado()!=pe.edu.upeu.bomerp.compras.ordencompra.entity.EstadoOrdenCompra.EMITIDA)throw new IllegalStateException("La compra no está EMITIDA");
        var esperado=new java.util.TreeMap<String,BigDecimal>();compra.getDetalles().forEach(d->esperado.merge(articulos.obtener(d.getArticuloId()).getCodigo(),BigDecimal.valueOf(d.getCantidad()),BigDecimal::add));
        var recibido=new java.util.TreeMap<String,BigDecimal>();for(var d:detalles){stock.validar(almacenId,d.getCodigoArticulo(),d.getCantidad());if(!articulos.obtenerPorCodigo(d.getCodigoArticulo()).getUnidadMedida().getCodigo().equals(d.getUnidadMedida()))throw new IllegalArgumentException("Unidad incorrecta");recibido.merge(d.getCodigoArticulo(),d.getCantidad(),BigDecimal::add);}
        if(!esperado.keySet().equals(recibido.keySet())||esperado.entrySet().stream().anyMatch(e->e.getValue().compareTo(recibido.get(e.getKey()))!=0))throw new IllegalArgumentException("La recepción debe coincidir con las líneas de la compra; no se admiten recepciones parciales en esta versión");
        if(pesajeKg==null||pesajeKg.signum()<=0||humedadPct==null||humedadPct.signum()<0||humedadPct.compareTo(BigDecimal.valueOf(100))>0||impurezaPct==null||impurezaPct.signum()<0||impurezaPct.compareTo(BigDecimal.valueOf(100))>0)throw new IllegalArgumentException("Revisa pesaje, humedad e impurezas");
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setTipoMovimiento(MovimientoInventario.TipoMovimiento.RECEPCION);
        movimiento.setAlmacenId(almacenId);
        movimiento.setOrdenCompraId(ordenCompraId);
        movimiento.setPesajeKg(pesajeKg);
        movimiento.setHumedadPct(humedadPct);
        movimiento.setImpurezaPct(impurezaPct);
        movimiento.setObservaciones(observaciones);
        detalles.forEach(movimiento::agregarDetalle);
        return movimientoRepository.save(movimiento);
    }

    /**
     * Paso 2: cierra el control de calidad. Si {@code aceptado} es true,
     * genera un {@link Lote} por cada detalle y recien ahi el articulo
     * entra a existencias -- si es false, el movimiento queda CONFIRMADO
     * como rechazo, sin generar ningun lote (mercancia devuelta al
     * proveedor, sin stock que mostrar).
     */
    public MovimientoInventario confirmarRecepcion(Long movimientoId, boolean aceptado) {
        MovimientoInventario movimiento = obtener(movimientoId);
        exigirTipoYEstado(movimiento, MovimientoInventario.TipoMovimiento.RECEPCION,
                MovimientoInventario.EstadoMovimiento.BORRADOR);

        var compra=compras.bloquear(movimiento.getOrdenCompraId()).orElseThrow();
        if(compra.getEstado()!=pe.edu.upeu.bomerp.compras.ordencompra.entity.EstadoOrdenCompra.EMITIDA)throw new IllegalStateException("Esta compra ya fue recibida o anulada");
        if(aceptado)compra.setEstado(pe.edu.upeu.bomerp.compras.ordencompra.entity.EstadoOrdenCompra.RECIBIDA);
        movimiento.setAceptado(aceptado);
        if (aceptado) {
            for (DetalleMovimiento detalle : movimiento.getDetalles()) {
                Lote lote = new Lote();
                lote.setCodigoArticulo(detalle.getCodigoArticulo());
                lote.setAlmacenId(movimiento.getAlmacenId());
                lote.setCantidadInicial(detalle.getCantidad());
                lote.setCantidadDisponible(detalle.getCantidad());
                lote.setMovimientoOrigenId(movimiento.getId());
                Lote guardado = loteRepository.save(lote);
                detalle.setLoteId(guardado.getId());
            }
        }
        movimiento.setEstado(MovimientoInventario.EstadoMovimiento.CONFIRMADO);
        movimiento.setFechaConfirmacion(LocalDateTime.now());
        return movimientoRepository.save(movimiento);
    }

    // ---------------------------------------------------------------
    // Despacho (salida, ligada a un pedido de ventas)
    // ---------------------------------------------------------------

    /**
     * Paso 1: registra el despacho asociado a un pedido de ventas ya
     * aprobado, con la seleccion de lotes y el vehiculo asignado. Se
     * valida (y reserva de inmediato) que cada lote elegido tenga
     * cantidad suficiente -- a diferencia de Produccion, aqui el lote ya
     * viene elegido por el operario de almacen, no hay que calcular
     * disponibilidad agregada.
     */
    public MovimientoInventario registrarDespacho(Long almacenId, Long pedidoVentaId,
                                                    String vehiculoPlaca, String observaciones,
                                                    List<DetalleMovimiento> detalles) {
        for (DetalleMovimiento detalle : detalles) {
            if (detalle.getLoteId() == null) {
                throw new IllegalArgumentException("Todo detalle de despacho debe especificar un lote");
            }
            Lote lote = obtenerLote(detalle.getLoteId());
            if (!lote.getAlmacenId().equals(almacenId)||!lote.getCodigoArticulo().equals(detalle.getCodigoArticulo())||detalle.getCantidad().signum()<=0||lote.getCantidadDisponible().compareTo(detalle.getCantidad()) < 0) {
                throw new IllegalStateException("El lote " + lote.getId() + " no tiene stock suficiente");
            }
        }

        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setTipoMovimiento(MovimientoInventario.TipoMovimiento.DESPACHO);
        movimiento.setAlmacenId(almacenId);
        movimiento.setPedidoVentaId(pedidoVentaId);
        movimiento.setVehiculoPlaca(vehiculoPlaca);
        movimiento.setObservaciones(observaciones);
        detalles.forEach(movimiento::agregarDetalle);
        return movimientoRepository.save(movimiento);
    }

    /**
     * Paso 2: confirma la entrega fisica. Recien aqui se descuenta la
     * cantidad de cada lote -- si la entrega se cancela antes de este
     * paso, el stock nunca se vio afectado.
     */
    public MovimientoInventario confirmarEntrega(Long movimientoId) {
        MovimientoInventario movimiento = obtener(movimientoId);
        exigirTipoYEstado(movimiento, MovimientoInventario.TipoMovimiento.DESPACHO,
                MovimientoInventario.EstadoMovimiento.BORRADOR);

        for (DetalleMovimiento detalle : movimiento.getDetalles()) {
            stock.consumir(movimiento.getAlmacenId(),detalle.getCodigoArticulo(),detalle.getCantidad(),detalle.getLoteId());
        }

        movimiento.setEstado(MovimientoInventario.EstadoMovimiento.CONFIRMADO);
        movimiento.setFechaConfirmacion(LocalDateTime.now());
        return movimientoRepository.save(movimiento);
    }

    /** Registra una devolución total vinculada al despacho original y devuelve las cantidades a sus lotes. */
    public MovimientoInventario registrarDevolucion(Long pedidoId, String motivo) {
        var despachos = movimientoRepository.findByPedidoVentaId(pedidoId).stream()
                .filter(m -> m.getTipoMovimiento() == MovimientoInventario.TipoMovimiento.DESPACHO
                        && m.getEstado() == MovimientoInventario.EstadoMovimiento.CONFIRMADO)
                .toList();
        if (despachos.size() != 1) {
            throw new IllegalStateException("No se encontró un despacho confirmado único para esta venta");
        }
        var despacho = despachos.get(0);
        var devolucion = new MovimientoInventario();
        devolucion.setTipoMovimiento(MovimientoInventario.TipoMovimiento.DEVOLUCION);
        devolucion.setAlmacenId(despacho.getAlmacenId());
        devolucion.setPedidoVentaId(pedidoId);
        devolucion.setObservaciones(motivo);
        for (var linea : despacho.getDetalles()) {
            var lote = obtenerLote(linea.getLoteId());
            lote.reingresar(linea.getCantidad());
            var devuelta = new DetalleMovimiento();
            devuelta.setCodigoArticulo(linea.getCodigoArticulo());
            devuelta.setCantidad(linea.getCantidad());
            devuelta.setLoteId(linea.getLoteId());
            devuelta.setUnidadMedida(linea.getUnidadMedida());
            devolucion.agregarDetalle(devuelta);
        }
        devolucion.setEstado(MovimientoInventario.EstadoMovimiento.CONFIRMADO);
        devolucion.setFechaConfirmacion(LocalDateTime.now());
        return movimientoRepository.save(devolucion);
    }

    public MovimientoInventario anular(Long movimientoId) {
        MovimientoInventario movimiento = obtener(movimientoId);
        if (movimiento.getEstado() == MovimientoInventario.EstadoMovimiento.CONFIRMADO) {
            throw new IllegalStateException("No se puede anular un movimiento ya CONFIRMADO");
        }
        movimiento.setEstado(MovimientoInventario.EstadoMovimiento.ANULADO);
        return movimientoRepository.save(movimiento);
    }

    // ---------------------------------------------------------------
    // Consultas
    // ---------------------------------------------------------------

    /**
     * Cantidad exacta disponible de un articulo en un almacen, calculada
     * en tiempo real como la suma de sus lotes DISPONIBLES. Este es el
     * metodo que Ventas y Produccion llaman antes de comprometer un
     * pedido o iniciar una orden -- Inventario nunca "reserva" nada aqui,
     * solo informa la verdad del momento.
     */
    @Transactional(readOnly = true)
    public BigDecimal consultarDisponible(Long almacenId, String codigoArticulo) {
        return loteRepository.sumarDisponible(codigoArticulo, almacenId);
    }

    @Transactional(readOnly = true)
    public List<Lote> listarLotesDisponibles(Long almacenId, String codigoArticulo) {
        return loteRepository.findByCodigoArticuloAndAlmacenIdAndEstado(
                codigoArticulo, almacenId, Lote.EstadoLote.DISPONIBLE);
    }

    @Transactional(readOnly = true)
    public MovimientoInventario obtener(Long id) {
        return movimientoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Movimiento no encontrado: " + id));
    }

    @Transactional(readOnly = true)
    public List<MovimientoInventario> listar(MovimientoInventario.TipoMovimiento tipo) {
        return tipo == null ? movimientoRepository.findAll() : movimientoRepository.findByTipoMovimiento(tipo);
    }

    @Transactional(readOnly = true)
    public Lote obtenerLote(Long loteId) {
        return loteRepository.findById(loteId)
                .orElseThrow(() -> new IllegalArgumentException("Lote no encontrado: " + loteId));
    }

    private void exigirTipoYEstado(MovimientoInventario movimiento,
                                    MovimientoInventario.TipoMovimiento tipoEsperado,
                                    MovimientoInventario.EstadoMovimiento estadoEsperado) {
        if (movimiento.getTipoMovimiento() != tipoEsperado) {
            throw new IllegalStateException("El movimiento " + movimiento.getId() + " no es de tipo " + tipoEsperado);
        }
        if (movimiento.getEstado() != estadoEsperado) {
            throw new IllegalStateException("El movimiento debe estar " + estadoEsperado
                    + " (actual: " + movimiento.getEstado() + ")");
        }
    }
}
