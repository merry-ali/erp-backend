package pe.edu.upeu.bomerp.ventas.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

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

    public List<Pedido> listar() {
        return repo.findAll();
    }

    public Pedido aprobar(Long id) {
        var p = repo.bloquear(id).orElseThrow();
        if (!p.getEstado().equals("BORRADOR")) {
            throw new IllegalStateException("Solo se aprueba un borrador");
        }

        var cantidades = new TreeMap<String, BigDecimal>();
        p.getDetalles().forEach(d -> 
            cantidades.merge(d.getCodigoArticulo(), d.getCantidad(), BigDecimal::add)
        );

        cantidades.forEach((c, n) -> {
            if (inventario.consultarDisponible(p.getAlmacenId(), c).compareTo(n) < 0) {
                throw new IllegalStateException("Stock insuficiente para " + c);
            }
        });

        p.setEstado("APROBADO");
        return p;
    }

    public Pedido entregar(Long id, String vehiculo) {
        return despachar(id, vehiculo, false);
    }

    public Pedido ventaRapida(PedidoRequest request, String sub) {
        Pedido pedido = crear(request, sub);
        aprobar(pedido.getId());
        return despachar(pedido.getId(), null, true);
    }

    private Pedido despachar(Long id, String vehiculo, boolean mostrador) {
        var p = repo.bloquear(id).orElseThrow();
        if (!p.getEstado().equals("APROBADO")) {
            throw new IllegalStateException("Solo se entrega un pedido aprobado");
        }
        if (!mostrador && (vehiculo == null || vehiculo.isBlank())) {
            throw new IllegalArgumentException("Indica la placa del vehículo");
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

        var movimiento = inventario.registrarDespacho(p.getAlmacenId(), p.getId(), vehiculo, "Despacho de pedido aprobado", detalles);
        inventario.confirmarEntrega(movimiento.getId());
        
        p.setVehiculo(vehiculo);
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
