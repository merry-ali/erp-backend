package pe.edu.upeu.bomerp.ventas.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pe.edu.upeu.bomerp.articulo.model.Articulo;
import pe.edu.upeu.bomerp.articulo.service.ArticuloService;
import pe.edu.upeu.bomerp.administracion.model.Almacen;
import pe.edu.upeu.bomerp.administracion.repository.AlmacenRepository;
import pe.edu.upeu.bomerp.personas.persona.entity.RolPersona;
import pe.edu.upeu.bomerp.personas.persona.service.PersonaService;
import pe.edu.upeu.bomerp.exception.ResourceNotFoundException;
import pe.edu.upeu.bomerp.ventas.entity.CotizacionVenta;
import pe.edu.upeu.bomerp.ventas.entity.DetalleCotizacionVenta;
import pe.edu.upeu.bomerp.ventas.entity.DetallePedido;
import pe.edu.upeu.bomerp.ventas.entity.Pedido;
import pe.edu.upeu.bomerp.ventas.repository.CotizacionVentaRepository;
import pe.edu.upeu.bomerp.ventas.repository.PedidoRepository;
import pe.edu.upeu.bomerp.ventas.web.dto.ConvertirCotizacionRequest;
import pe.edu.upeu.bomerp.ventas.web.dto.CrearCotizacionRequest;

@Service
@RequiredArgsConstructor
@Transactional
public class CotizacionVentaService {

    private final CotizacionVentaRepository cotizaciones;
    private final PedidoRepository pedidos;
    private final PersonaService personas;
    private final ArticuloService articulos;
    private final AlmacenRepository almacenes;

    public CotizacionVenta crear(CrearCotizacionRequest request) {
        validarCliente(request.clienteId());

        CotizacionVenta cotizacion = new CotizacionVenta();
        cotizacion.setClienteId(request.clienteId());
        cotizacion.setFormaPago(request.formaPago());
        cotizacion.setPlazoPago(request.plazoPago());

        BigDecimal total = BigDecimal.ZERO;
        for (CrearCotizacionRequest.Linea linea : request.detalles()) {
            var articulo = validarProductoVendible(linea.codigoArticulo());
            Long almacenId = resolverAlmacen(linea.almacenId(), articulo);

            DetalleCotizacionVenta detalle = new DetalleCotizacionVenta();
            detalle.setCotizacion(cotizacion);
            detalle.setNumeroLinea(cotizacion.getDetalles().size() + 1);
            detalle.setCodigoArticulo(linea.codigoArticulo());
            detalle.setAlmacenId(almacenId);
            detalle.setCantidad(linea.cantidad());
            detalle.setPrecioUnitario(linea.precioUnitario());
            detalle.setDescuento(linea.descuento());
            detalle.setSubtotal(calcularSubtotal(
                    linea.cantidad(), linea.precioUnitario(), linea.descuento()));

            total = total.add(detalle.getSubtotal());
            cotizacion.getDetalles().add(detalle);
        }

        cotizacion.setTotal(total);
        return cotizaciones.save(cotizacion);
    }

    @Transactional(readOnly = true)
    public List<CotizacionVenta> listar() {
        return cotizaciones.findAll();
    }

    public CotizacionVenta enviar(Long id) {
        CotizacionVenta cotizacion = obtener(id);
        cambiarEstado(cotizacion, "BORRADOR", "ENVIADA");
        return cotizacion;
    }

    public CotizacionVenta aceptar(Long id) {
        CotizacionVenta cotizacion = obtener(id);
        cambiarEstado(cotizacion, "ENVIADA", "ACEPTADA");
        return cotizacion;
    }

    public CotizacionVenta rechazar(Long id) {
        CotizacionVenta cotizacion = obtener(id);
        cambiarEstado(cotizacion, "ENVIADA", "RECHAZADA");
        return cotizacion;
    }

    public Pedido convertirEnPedido(Long id, ConvertirCotizacionRequest request, String usuario) {
        CotizacionVenta cotizacion = obtener(id);
        if (!"ACEPTADA".equals(cotizacion.getEstado())) {
            throw new IllegalStateException("Solo se convierte una cotización aceptada");
        }
        if (cotizacion.getPedidoVentaId() != null) {
            throw new IllegalStateException("La cotización ya fue convertida en pedido");
        }

        validarCliente(cotizacion.getClienteId());

        Pedido pedido = new Pedido();
        pedido.setClienteId(cotizacion.getClienteId());
        pedido.setAlmacenId(cotizacion.getDetalles().stream().map(DetalleCotizacionVenta::getAlmacenId)
                .filter(java.util.Objects::nonNull).findFirst().orElse(request.almacenId()));
        pedido.setCotizacionVentaId(cotizacion.getId());
        pedido.setMoneda(cotizacion.getMoneda());
        pedido.setTotal(cotizacion.getTotal());
        pedido.setFormaPagoOfrecida(cotizacion.getFormaPago());
        pedido.setPlazoPagoOfrecido(cotizacion.getPlazoPago());
        pedido.setOperadorSub(usuario);
        String modalidad = request.tipoEntrega() == null || request.tipoEntrega().isBlank()
                ? "LOCAL" : request.tipoEntrega().trim().toUpperCase();
        if (!"LOCAL".equals(modalidad) || (request.vehiculo() != null && !request.vehiculo().isBlank())) {
            throw new IllegalArgumentException("Los pedidos se entregan en el mismo local y no requieren vehículo");
        }
        pedido.setTipoEntrega("LOCAL");
        pedido.setVehiculo(null);

        for (DetalleCotizacionVenta linea : cotizacion.getDetalles()) {
            DetallePedido detalle = new DetallePedido();
            detalle.setPedido(pedido);
            detalle.setNumeroLinea(linea.getNumeroLinea());
            detalle.setCodigoArticulo(linea.getCodigoArticulo());
            detalle.setAlmacenId(linea.getAlmacenId() == null ? request.almacenId() : linea.getAlmacenId());
            detalle.setCantidad(linea.getCantidad());
            detalle.setPrecio(linea.getPrecioUnitario());
            detalle.setDescuento(linea.getDescuento());
            detalle.setSubtotal(linea.getSubtotal());
            pedido.getDetalles().add(detalle);
        }

        Pedido pedidoGuardado = pedidos.save(pedido);
        cotizacion.setPedidoVentaId(pedidoGuardado.getId());
        return pedidoGuardado;
    }

    private CotizacionVenta obtener(Long id) {
        return cotizaciones.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cotización no encontrada: " + id));
    }

    private void cambiarEstado(CotizacionVenta cotizacion, String estadoActual, String estadoNuevo) {
        if (!estadoActual.equals(cotizacion.getEstado())) {
            throw new IllegalStateException("La cotización debe estar en estado " + estadoActual);
        }
        cotizacion.setEstado(estadoNuevo);
    }

    private void validarCliente(Long clienteId) {
        var cliente = personas.obtenerPersona(clienteId);
        if (!cliente.isActivo()
                || cliente.getRoles() == null
                || !cliente.getRoles().contains(RolPersona.CLIENTE)) {
            throw new IllegalArgumentException("El cliente debe estar activo y tener el rol CLIENTE");
        }
    }

    private Articulo validarProductoVendible(String codigoArticulo) {
        Articulo articulo = articulos.obtenerPorCodigo(codigoArticulo);
        if (!articulo.isActivo() || (articulo.getTipoArticulo() != Articulo.TipoArticulo.PRODUCTO_TERMINADO
                && articulo.getTipoArticulo() != Articulo.TipoArticulo.MATERIA_PRIMA)) {
            throw new IllegalArgumentException("Solo se pueden cotizar materias primas y productos terminados activos");
        }
        return articulo;
    }

    private Long resolverAlmacen(Long solicitado, Articulo articulo) {
        Almacen.TipoAlmacen tipo = articulo.getTipoArticulo() == Articulo.TipoArticulo.MATERIA_PRIMA
                ? Almacen.TipoAlmacen.MATERIA_PRIMA : Almacen.TipoAlmacen.PRODUCTO_TERMINADO;
        Long id = solicitado;
        if (id == null) {
            var opciones = almacenes.findByTipoAndActivoTrue(tipo);
            if (opciones.isEmpty()) opciones = almacenes.findByTipoAndActivoTrue(Almacen.TipoAlmacen.GENERAL);
            if (opciones.size() != 1) {
                throw new IllegalArgumentException("Selecciona el almacén de " + tipo.name().toLowerCase().replace('_', ' ')
                        + " para el artículo " + articulo.getCodigo());
            }
            id = opciones.get(0).getId();
        }
        Almacen almacen = almacenes.findById(id).filter(Almacen::isActivo)
                .orElseThrow(() -> new IllegalArgumentException("El almacén seleccionado no existe o está inactivo"));
        if (almacen.getTipo() != tipo && almacen.getTipo() != Almacen.TipoAlmacen.GENERAL) {
            throw new IllegalArgumentException("El artículo " + articulo.getCodigo() + " requiere un almacén "
                    + tipo.name().toLowerCase().replace('_', ' '));
        }
        return id;
    }

    private BigDecimal calcularSubtotal(BigDecimal cantidad, BigDecimal precio, BigDecimal descuento) {
        BigDecimal factorDescuento = BigDecimal.ONE.subtract(
                descuento.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP));
        return cantidad.multiply(precio).multiply(factorDescuento)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
