package pe.edu.upeu.bomerp.ventas.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.bomerp.ventas.entity.DetallePedido;
import lombok.RequiredArgsConstructor;
import pe.edu.upeu.bomerp.articulo.service.ArticuloService;
import pe.edu.upeu.bomerp.administracion.model.Almacen;
import pe.edu.upeu.bomerp.administracion.repository.AlmacenRepository;
import pe.edu.upeu.bomerp.inventario.model.DetalleMovimiento;
import pe.edu.upeu.bomerp.inventario.service.InventarioService;
import pe.edu.upeu.bomerp.personas.persona.entity.RolPersona;
import pe.edu.upeu.bomerp.personas.persona.service.PersonaService;

import pe.edu.upeu.bomerp.ventas.entity.Pedido;
import pe.edu.upeu.bomerp.ventas.entity.PagoPedido;
import pe.edu.upeu.bomerp.ventas.repository.PedidoRepository;
import pe.edu.upeu.bomerp.ventas.web.dto.EntregaPedidoRequest;
import pe.edu.upeu.bomerp.ventas.web.dto.PedidoRequest;

@Service
@RequiredArgsConstructor
@Transactional
public class PedidoService {

    private final PedidoRepository repo;
    private final PersonaService personas;
    private final ArticuloService articulos;
    private final InventarioService inventario;
    private final AlmacenRepository almacenes;

    public Pedido crear(PedidoRequest r, String sub) {
        var cliente = personas.obtenerPersona(r.clienteId());
        if (!cliente.isActivo()
                || cliente.getRoles() == null
                || !cliente.getRoles().contains(RolPersona.CLIENTE)) {
            throw new IllegalArgumentException("El cliente debe estar activo y tener el rol CLIENTE");
        }

        var p = new Pedido();
        p.setClienteId(r.clienteId());
        p.setAlmacenId(r.almacenId());
        p.setOperadorSub(sub);
        aplicarEntregaPrevista(p, r.tipoEntrega(), r.vehiculo());
        
        BigDecimal total = BigDecimal.ZERO;

        for (var x : r.detalles()) {
            var a = articulos.obtenerPorCodigo(x.codigoArticulo());
            Long almacenLinea = validarAlmacenArticulo(x.almacenId() == null ? r.almacenId() : x.almacenId(), a);
            
            var d = new DetallePedido();
            d.setPedido(p);
            d.setNumeroLinea(p.getDetalles().size() + 1);
            d.setCodigoArticulo(x.codigoArticulo());
            d.setAlmacenId(almacenLinea);
            d.setCantidad(x.cantidad());
            d.setPrecio(x.precio());
            d.setDescuento(x.descuento());
            
            d.setSubtotal(
                x.cantidad()
                 .multiply(x.precio())
                 .multiply(BigDecimal.ONE.subtract(x.descuento().divide(BigDecimal.valueOf(100))))
                 .setScale(2, RoundingMode.HALF_UP)
            );
            
            total = total.add(d.getSubtotal());
            p.getDetalles().add(d);
        }

        p.setTotal(total);
        return repo.save(p);
    }

    public Pedido actualizarBorrador(Long id, PedidoRequest r, String sub) {
        var p = repo.bloquear(id).orElseThrow();
        if (!"BORRADOR".equals(p.getEstado())) {
            throw new IllegalStateException("Solo se puede revisar un pedido que siga como borrador");
        }
        var cliente = personas.obtenerPersona(r.clienteId());
        if (!cliente.isActivo() || cliente.getRoles() == null || !cliente.getRoles().contains(RolPersona.CLIENTE)) {
            throw new IllegalArgumentException("El cliente debe estar activo y tener el rol CLIENTE");
        }
        p.setClienteId(r.clienteId());
        p.setAlmacenId(r.almacenId());
        p.setOperadorSub(sub);
        aplicarEntregaPrevista(p, r.tipoEntrega(), r.vehiculo());
        p.getDetalles().clear();
        BigDecimal total = BigDecimal.ZERO;
        for (var x : r.detalles()) {
            var articulo = articulos.obtenerPorCodigo(x.codigoArticulo());
            Long almacenLinea = validarAlmacenArticulo(x.almacenId() == null ? r.almacenId() : x.almacenId(), articulo);
            var detalle = new DetallePedido();
            detalle.setPedido(p);
            detalle.setNumeroLinea(p.getDetalles().size() + 1);
            detalle.setCodigoArticulo(x.codigoArticulo());
            detalle.setAlmacenId(almacenLinea);
            detalle.setCantidad(x.cantidad());
            detalle.setPrecio(x.precio());
            detalle.setDescuento(x.descuento());
            detalle.setSubtotal(x.cantidad().multiply(x.precio())
                    .multiply(BigDecimal.ONE.subtract(x.descuento().divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP)))
                    .setScale(2, RoundingMode.HALF_UP));
            total = total.add(detalle.getSubtotal());
            p.getDetalles().add(detalle);
        }
        p.setTotal(total);
        return repo.save(p);
    }

    public List<Pedido> listar() {
        return repo.findAll();
    }

    private void aplicarEntregaPrevista(Pedido pedido, String tipoEntrega, String vehiculo) {
        String modalidad = tipoEntrega == null || tipoEntrega.isBlank() ? "LOCAL" : tipoEntrega.trim().toUpperCase();
        if (!"LOCAL".equals(modalidad) || (vehiculo != null && !vehiculo.isBlank())) {
            throw new IllegalArgumentException("Los pedidos se entregan en el mismo local y no requieren vehículo");
        }
        pedido.setTipoEntrega("LOCAL");
        pedido.setVehiculo(null);
    }

    public Pedido aprobar(Long id) {
        var p = repo.bloquear(id).orElseThrow();
        if (!p.getEstado().equals("BORRADOR")) {
            throw new IllegalStateException("Solo se aprueba un borrador");
        }
        BigDecimal pagado = p.getMontoPagado() == null ? BigDecimal.ZERO : p.getMontoPagado();
        if (!"PAGADO".equals(p.getEstadoPago()) || pagado.compareTo(p.getTotal()) < 0) {
            throw new IllegalStateException("No se puede aprobar: registra y verifica el pago completo del pedido primero. Pagado: "
                    + pagado + " de " + p.getTotal());
        }

        var cantidades = new TreeMap<Long, TreeMap<String, BigDecimal>>();
        p.getDetalles().forEach(d -> cantidades.computeIfAbsent(almacenDetalle(p, d), k -> new TreeMap<>())
                .merge(d.getCodigoArticulo(), d.getCantidad(), BigDecimal::add));

        cantidades.forEach((almacenId, porArticulo) -> porArticulo.forEach((codigo, n) -> {
            BigDecimal disponible = inventario.consultarDisponible(almacenId, codigo);
            if (disponible.compareTo(n) < 0) {
                var nombreAlmacen = almacenes.findById(almacenId)
                        .map(Almacen::getNombre)
                        .orElse("desconocido");
                var stockEnOtrosAlmacenes = almacenes.findAll().stream()
                        .filter(Almacen::isActivo)
                        .filter(almacen -> !almacen.getId().equals(almacenId))
                        .map(almacen -> new java.util.AbstractMap.SimpleEntry<>(almacen,
                                inventario.consultarDisponible(almacen.getId(), codigo)))
                        .filter(entrada -> entrada.getValue().signum() > 0)
                        .map(entrada -> entrada.getValue().stripTrailingZeros().toPlainString()
                                + " en almacén #" + entrada.getKey().getId() + " (" + entrada.getKey().getNombre() + ")")
                        .collect(java.util.stream.Collectors.joining(", "));
                var sugerencia = stockEnOtrosAlmacenes.isEmpty()
                        ? " No hay existencias disponibles del artículo en otros almacenes."
                        : " Existencias encontradas en otros almacenes: " + stockEnOtrosAlmacenes
                                + ". Revisa el almacén asignado a la línea del pedido.";
                throw new IllegalStateException("Stock insuficiente para " + codigo + " en almacén #"
                        + almacenId + " (" + nombreAlmacen + "). Pedido: " + n + "; disponible: " + disponible + "."
                        + sugerencia);
            }
        }));

        p.setEstado("APROBADO");
        return p;
    }

    public Pedido entregar(Long id, String tipoEntrega, String vehiculo) {
        return despachar(id, tipoEntrega, vehiculo, false);
    }

    /**
     * Confirma los lotes elegidos por Almacén. La suma por artículo y almacén
     * debe coincidir con el pedido; todos los movimientos se confirman dentro
     * de la transacción del pedido.
     */
    public Pedido entregarSeleccion(Long id, EntregaPedidoRequest request) {
        var pedido = repo.bloquear(id).orElseThrow(() -> new IllegalArgumentException("Pedido inexistente"));
        if (!"APROBADO".equals(pedido.getEstado())) {
            throw new IllegalStateException("Solo se entrega un pedido aprobado");
        }
        if (request == null || request.detalles() == null || request.detalles().isEmpty()) {
            throw new IllegalArgumentException("Selecciona los lotes y cantidades para el despacho");
        }

        if (request.tipoEntrega() == null || request.tipoEntrega().isBlank()
                || !"LOCAL".equalsIgnoreCase(request.tipoEntrega().trim())) {
            throw new IllegalArgumentException("Solo se permite la entrega en el mismo local");
        }
        if (request.vehiculo() != null && !request.vehiculo().isBlank()) {
            throw new IllegalArgumentException("La entrega en el local no debe incluir vehículo");
        }
        String modalidad = "LOCAL";
        String placa = null;

        var esperado = new TreeMap<Long, TreeMap<String, BigDecimal>>();
        for (var lineaPedido : pedido.getDetalles()) {
            sumarCantidad(esperado, almacenDetalle(pedido, lineaPedido), lineaPedido.getCodigoArticulo(), lineaPedido.getCantidad());
        }

        var seleccionado = new TreeMap<Long, TreeMap<String, BigDecimal>>();
        var cantidadPorLote = new TreeMap<Long, BigDecimal>();
        var detallesPorAlmacen = new TreeMap<Long, List<DetalleMovimiento>>();
        for (var linea : request.detalles()) {
            if (linea == null || linea.codigoArticulo() == null || linea.codigoArticulo().isBlank()
                    || linea.loteId() == null || linea.cantidad() == null || linea.cantidad().signum() <= 0) {
                throw new IllegalArgumentException("Selecciona un artículo, lote y cantidad positiva");
            }
            String codigo = linea.codigoArticulo().trim();
            var lote = inventario.obtenerLote(linea.loteId());
            Long almacenId = lote.getAlmacenId();
            if (!codigo.equals(lote.getCodigoArticulo())
                    || lote.getEstado() != pe.edu.upeu.bomerp.inventario.model.Lote.EstadoLote.DISPONIBLE
                    || !esperado.getOrDefault(almacenId, new TreeMap<>()).containsKey(codigo)) {
                throw new IllegalArgumentException("El lote seleccionado no corresponde al artículo y almacén asignados al pedido");
            }

            BigDecimal acumuladoLote = cantidadPorLote.merge(lote.getId(), linea.cantidad(), BigDecimal::add);
            if (acumuladoLote.compareTo(lote.getCantidadDisponible()) > 0) {
                throw new IllegalStateException("Stock insuficiente en el lote " + lote.getId());
            }
            sumarCantidad(seleccionado, almacenId, codigo, linea.cantidad());

            var detalle = new DetalleMovimiento();
            detalle.setCodigoArticulo(codigo);
            detalle.setLoteId(lote.getId());
            detalle.setCantidad(linea.cantidad());
            detalle.setUnidadMedida(articulos.obtenerPorCodigo(codigo).getUnidadMedida().getCodigo());
            detallesPorAlmacen.computeIfAbsent(almacenId, ignored -> new ArrayList<>()).add(detalle);
        }

        if (!cantidadesCoinciden(esperado, seleccionado)) {
            throw new IllegalArgumentException("Las cantidades seleccionadas por artículo y almacén deben coincidir exactamente con el pedido");
        }

        for (var entrada : detallesPorAlmacen.entrySet()) {
            var movimiento = inventario.registrarDespacho(entrada.getKey(), pedido.getId(), modalidad, placa,
                    "Lotes seleccionados por Almacén", entrada.getValue());
            inventario.confirmarEntrega(movimiento.getId());
        }

        pedido.setVehiculo(placa);
        pedido.setTipoEntrega(modalidad);
        pedido.setEstado("DESPACHADO");
        return pedido;
    }

    private void sumarCantidad(TreeMap<Long, TreeMap<String, BigDecimal>> cantidades,
                               Long almacenId, String codigo, BigDecimal cantidad) {
        cantidades.computeIfAbsent(almacenId, ignored -> new TreeMap<>())
                .merge(codigo, cantidad, BigDecimal::add);
    }

    private boolean cantidadesCoinciden(TreeMap<Long, TreeMap<String, BigDecimal>> esperado,
                                        TreeMap<Long, TreeMap<String, BigDecimal>> seleccionado) {
        if (!esperado.keySet().equals(seleccionado.keySet())) return false;
        for (var porAlmacen : esperado.entrySet()) {
            var recibido = seleccionado.get(porAlmacen.getKey());
            if (recibido == null || !porAlmacen.getValue().keySet().equals(recibido.keySet())) return false;
            for (var porArticulo : porAlmacen.getValue().entrySet()) {
                if (porArticulo.getValue().compareTo(recibido.get(porArticulo.getKey())) != 0) return false;
            }
        }
        return true;
    }

    public Pedido registrarPago(Long id, BigDecimal monto, String formaPago, String referencia) {
        var p = repo.bloquear(id).orElseThrow();
        if ("ANULADO".equals(p.getEstado()) || "DEVUELTO".equals(p.getEstado())) {
            throw new IllegalStateException("No se puede registrar un pago para un pedido anulado o devuelto");
        }
        if (monto == null) {
            throw new IllegalArgumentException("El monto del pago debe ser mayor a cero");
        }
        var importe = monto.setScale(2, RoundingMode.HALF_UP);
        if (importe.signum() <= 0) {
            throw new IllegalArgumentException("El monto del pago debe ser mayor a cero");
        }
        if (formaPago == null || formaPago.isBlank()) {
            throw new IllegalArgumentException("Selecciona el método de pago recibido");
        }
        var pagado = p.getMontoPagado() == null ? BigDecimal.ZERO : p.getMontoPagado();
        var saldo = p.getTotal().subtract(pagado);
        if (importe.compareTo(saldo) > 0) {
            throw new IllegalArgumentException("El pago supera el saldo pendiente de " + saldo);
        }
        pagado = pagado.add(importe).setScale(2, RoundingMode.HALF_UP);
        p.setMontoPagado(pagado);
        p.setEstadoPago(pagado.compareTo(p.getTotal()) >= 0 ? "PAGADO" : "PARCIAL");
        p.setFormaPagoReal(formaPago.trim());
        p.setFechaUltimoPago(LocalDateTime.now());
        var pago = new PagoPedido();
        pago.setPedido(p);
        pago.setMonto(importe);
        pago.setFormaPago(formaPago.trim());
        pago.setReferencia(referencia == null || referencia.isBlank() ? null : referencia.trim());
        p.getPagos().add(pago);
        return p;
    }

    public Pedido ventaRapida(PedidoRequest request, String sub, String nombreEmisor) {
        if (request.formaPagoReal() == null || request.formaPagoReal().isBlank()) {
            throw new IllegalArgumentException("Selecciona el método de pago recibido en mostrador");
        }
        for (var linea : request.detalles()) {
            if (articulos.obtenerPorCodigo(linea.codigoArticulo()).getTipoArticulo()
                    != pe.edu.upeu.bomerp.articulo.model.Articulo.TipoArticulo.PRODUCTO_TERMINADO) {
                throw new IllegalArgumentException("La venta rápida solo admite productos terminados");
            }
        }
        Pedido pedido = crear(request, sub);
        pedido.setVentaRapida(true);
        pedido.setFechaEmisionVoucher(LocalDateTime.now());
        pedido.setUsuarioEmisionVoucher(nombreEmisor);
        repo.save(pedido);
        registrarPago(pedido.getId(), pedido.getTotal(), request.formaPagoReal(), "Pago recibido en venta rápida");
        aprobar(pedido.getId());
        return despachar(pedido.getId(), "LOCAL", null, true);
    }

    private Pedido despachar(Long id, String tipoEntrega, String vehiculo, boolean mostrador) {
        var p = repo.bloquear(id).orElseThrow();
        if (!p.getEstado().equals("APROBADO")) {
            throw new IllegalStateException("Solo se entrega un pedido aprobado");
        }
        String modalidad = mostrador ? "LOCAL" : (tipoEntrega == null ? "" : tipoEntrega.trim().toUpperCase());
        if (!"LOCAL".equals(modalidad)) {
            throw new IllegalArgumentException("Solo se permite la entrega en el mismo local");
        }
        if (vehiculo != null && !vehiculo.isBlank()) {
            throw new IllegalArgumentException("La entrega en el local no debe incluir vehículo");
        }
        final String placa = null;

        var cantidadesPorAlmacen = new TreeMap<Long, TreeMap<String, BigDecimal>>();
        for (var d : p.getDetalles()) {
            Long almacenId = almacenDetalle(p, d);
            cantidadesPorAlmacen.computeIfAbsent(almacenId, k -> new TreeMap<>())
                    .merge(d.getCodigoArticulo(), d.getCantidad(), BigDecimal::add);
        }
        cantidadesPorAlmacen.forEach((almacenId, cantidades) -> {
            var detalles = new ArrayList<DetalleMovimiento>();
            cantidades.forEach((codigo, cantidad) -> {
                var restante = cantidad;
                for (var lote : inventario.listarLotesDisponibles(almacenId, codigo)) {
                    var q = restante.min(lote.getCantidadDisponible());
                    if (q.signum() > 0) {
                        var m = new DetalleMovimiento();
                        m.setCodigoArticulo(codigo);
                        m.setCantidad(q);
                        m.setLoteId(lote.getId());
                        m.setUnidadMedida(articulos.obtenerPorCodigo(codigo).getUnidadMedida().getCodigo());
                        detalles.add(m);
                        restante = restante.subtract(q);
                    }
                    if (restante.signum() == 0) break;
                }
                if (restante.signum() > 0) throw new IllegalStateException("Stock insuficiente al entregar para " + codigo + " en el almacén " + almacenId);
            });
            var movimiento = inventario.registrarDespacho(almacenId, p.getId(), modalidad, placa,
                    "Despacho de pedido aprobado", detalles);
            inventario.confirmarEntrega(movimiento.getId());
        });
        
        p.setVehiculo(placa);
        p.setTipoEntrega(modalidad);
        p.setEstado("DESPACHADO");
        return p;
    }

    public Pedido anular(Long id, String motivo) {
        var p = repo.bloquear(id).orElseThrow();
        if (!p.getEstado().equals("BORRADOR") && !p.getEstado().equals("APROBADO")) {
            throw new IllegalStateException("Solo se puede anular un borrador o pedido aprobado sin despachar");
        }
        if (motivo == null || motivo.isBlank()) throw new IllegalArgumentException("Indica el motivo de anulación");
        p.setMotivoAnulacion(motivo.trim());
        p.setEstado("ANULADO");
        return p;
    }

    public Pedido revertir(Long id, String motivo) {
        var p = repo.bloquear(id).orElseThrow();
        if (!"DESPACHADO".equals(p.getEstado())) {
            throw new IllegalStateException("Solo se puede revertir una venta despachada");
        }
        if (motivo == null || motivo.isBlank()) throw new IllegalArgumentException("Indica el motivo de reversión");
        inventario.registrarDevolucion(id, motivo.trim());
        p.setMotivoReversion(motivo.trim());
        p.setEstado("DEVUELTO");
        return p;
    }

    private Long almacenDetalle(Pedido pedido, DetallePedido detalle) {
        return detalle.getAlmacenId() == null ? pedido.getAlmacenId() : detalle.getAlmacenId();
    }

    private Long validarAlmacenArticulo(Long almacenId, pe.edu.upeu.bomerp.articulo.model.Articulo articulo) {
        if (!articulo.isActivo() || (articulo.getTipoArticulo() != pe.edu.upeu.bomerp.articulo.model.Articulo.TipoArticulo.PRODUCTO_TERMINADO
                && articulo.getTipoArticulo() != pe.edu.upeu.bomerp.articulo.model.Articulo.TipoArticulo.MATERIA_PRIMA)) {
            throw new IllegalArgumentException("Solo se venden materias primas y productos terminados activos");
        }
        var almacen = almacenes.findById(almacenId).filter(Almacen::isActivo)
                .orElseThrow(() -> new IllegalArgumentException("El almacén seleccionado no existe o está inactivo"));
        var esperado = articulo.getTipoArticulo() == pe.edu.upeu.bomerp.articulo.model.Articulo.TipoArticulo.MATERIA_PRIMA
                ? Almacen.TipoAlmacen.MATERIA_PRIMA : Almacen.TipoAlmacen.PRODUCTO_TERMINADO;
        if (almacen.getTipo() != esperado && almacen.getTipo() != Almacen.TipoAlmacen.GENERAL) {
            throw new IllegalArgumentException("El artículo " + articulo.getCodigo() + " requiere un almacén "
                    + esperado.name().toLowerCase().replace('_', ' '));
        }
        return almacenId;
    }

}
