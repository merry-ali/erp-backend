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
import pe.edu.upeu.bomerp.inventario.model.DetalleMovimiento;
import pe.edu.upeu.bomerp.inventario.service.InventarioService;
import pe.edu.upeu.bomerp.personas.persona.entity.RolPersona;
import pe.edu.upeu.bomerp.personas.persona.service.PersonaService;

import pe.edu.upeu.bomerp.ventas.entity.Pedido;
import pe.edu.upeu.bomerp.ventas.entity.PagoPedido;
import pe.edu.upeu.bomerp.ventas.repository.PedidoRepository;
import pe.edu.upeu.bomerp.ventas.web.dto.PedidoRequest;

@Service
@RequiredArgsConstructor
@Transactional
public class PedidoService {

    private final PedidoRepository repo;
    private final PersonaService personas;
    private final ArticuloService articulos;
    private final InventarioService inventario;

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
            if (a.getTipoArticulo() != pe.edu.upeu.bomerp.articulo.model.Articulo.TipoArticulo.PRODUCTO_TERMINADO) {
                throw new IllegalArgumentException("Solo se venden productos terminados");
            }
            
            var d = new DetallePedido();
            d.setPedido(p);
            d.setNumeroLinea(p.getDetalles().size() + 1);
            d.setCodigoArticulo(x.codigoArticulo());
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
            if (articulo.getTipoArticulo() != pe.edu.upeu.bomerp.articulo.model.Articulo.TipoArticulo.PRODUCTO_TERMINADO) {
                throw new IllegalArgumentException("Solo se venden productos terminados");
            }
            var detalle = new DetallePedido();
            detalle.setPedido(p);
            detalle.setNumeroLinea(p.getDetalles().size() + 1);
            detalle.setCodigoArticulo(x.codigoArticulo());
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
        String modalidad = tipoEntrega == null ? "" : tipoEntrega.trim().toUpperCase();
        if (modalidad.isEmpty()) {
            pedido.setTipoEntrega(null);
            pedido.setVehiculo(null);
            return;
        }
        if (!"LOCAL".equals(modalidad) && !"VEHICULO".equals(modalidad)) {
            throw new IllegalArgumentException("Selecciona entrega en vehículo o en el local");
        }
        if ("VEHICULO".equals(modalidad) && vehiculo != null && vehiculo.length() > 30) {
            throw new IllegalArgumentException("La placa no puede superar 30 caracteres");
        }
        pedido.setTipoEntrega(modalidad);
        pedido.setVehiculo("VEHICULO".equals(modalidad) && vehiculo != null && !vehiculo.isBlank()
                ? vehiculo.trim().toUpperCase() : null);
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

        var cantidades = new TreeMap<String, BigDecimal>();
        p.getDetalles().forEach(d -> 
            cantidades.merge(d.getCodigoArticulo(), d.getCantidad(), BigDecimal::add)
        );

        cantidades.forEach((c, n) -> {
            BigDecimal disponible = inventario.consultarDisponible(p.getAlmacenId(), c);
            if (disponible.compareTo(n) < 0) {
                throw new IllegalStateException("Stock insuficiente para " + c + " en el almacén "
                        + p.getAlmacenId() + ". Pedido: " + n + "; disponible: " + disponible);
            }
        });

        p.setEstado("APROBADO");
        return p;
    }

    public Pedido entregar(Long id, String tipoEntrega, String vehiculo) {
        return despachar(id, tipoEntrega, vehiculo, false);
    }

    public Pedido entregar(Long id, String vehiculo) {
        return entregar(id, "VEHICULO", vehiculo);
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
        if (!"LOCAL".equals(modalidad) && !"VEHICULO".equals(modalidad)) {
            throw new IllegalArgumentException("Selecciona entrega en vehículo o en el local");
        }
        if ("VEHICULO".equals(modalidad) && (vehiculo == null || vehiculo.isBlank())) {
            throw new IllegalArgumentException("Indica la placa del vehículo");
        }
        if ("LOCAL".equals(modalidad)) {
            vehiculo = null;
        }

        var detalles = new ArrayList<DetalleMovimiento>();

        for (var d : p.getDetalles()) {
            var restante = d.getCantidad();
            for (var lote : inventario.listarLotesDisponibles(p.getAlmacenId(), d.getCodigoArticulo())) {
                var q = restante.min(lote.getCantidadDisponible());
                if (q.signum() > 0) {
                    var m = new DetalleMovimiento();
                    m.setCodigoArticulo(d.getCodigoArticulo());
                    m.setCantidad(q);
                    m.setLoteId(lote.getId());
                    m.setUnidadMedida(articulos.obtenerPorCodigo(d.getCodigoArticulo()).getUnidadMedida().getCodigo());
                    detalles.add(m);
                    restante = restante.subtract(q);
                }
                if (restante.signum() == 0) break;
            }
            if (restante.signum() > 0) {
                throw new IllegalStateException("Stock insuficiente al entregar");
            }
        }

        var movimiento = inventario.registrarDespacho(p.getAlmacenId(), p.getId(), modalidad, vehiculo,
                "Despacho de pedido aprobado", detalles);
        inventario.confirmarEntrega(movimiento.getId());
        
        p.setVehiculo(vehiculo);
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

}
