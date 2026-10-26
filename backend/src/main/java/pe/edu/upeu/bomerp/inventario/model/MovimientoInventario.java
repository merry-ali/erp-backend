package pe.edu.upeu.bomerp.inventario.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Cabecera de un movimiento fisico de almacen: RECEPCION (entrada, ligada
 * a una orden de compra) o DESPACHO (salida, ligada a un pedido de
 * ventas). Este modulo no toma decisiones comerciales -- solo ejecuta y
 * refleja lo que fisicamente entra o sale del almacen.
 * <p>
 * Los campos de control de calidad (pesaje/humedad/impureza) y
 * {@code aceptado} solo aplican a RECEPCION; {@code vehiculoPlaca} solo
 * aplica a DESPACHO. Se modelan en la misma cabecera (en vez de dos
 * tablas separadas) porque ambos comparten el mismo ciclo de vida
 * BORRADOR -> CONFIRMADO/ANULADO y el mismo detalle de articulos/lotes.
 */
@Entity
@Table(name = "movimiento_inventario", schema = "bomerp")
@Getter
@Setter
@NoArgsConstructor
public class MovimientoInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "movimiento_inventario_seq")
    @SequenceGenerator(name = "movimiento_inventario_seq", sequenceName = "movimiento_inventario_seq", allocationSize = 1)
    private Long id;
    @Version
    private Long revision;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_movimiento", length = 15, nullable = false)
    private TipoMovimiento tipoMovimiento;

    /** Referencia logica al almacen de Administracion. */
    @NotNull
    @Column(name = "almacen_id", nullable = false)
    private Long almacenId;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 15, nullable = false)
    private EstadoMovimiento estado = EstadoMovimiento.BORRADOR;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    @Column(name = "fecha_confirmacion")
    private LocalDateTime fechaConfirmacion;

    /** Solo RECEPCION: referencia logica a la orden de compra emitida por Compras. */
    @Column(name = "orden_compra_id")
    private Long ordenCompraId;

    /** Solo DESPACHO: referencia logica al pedido aprobado por Ventas. */
    @Column(name = "pedido_venta_id")
    private Long pedidoVentaId;

    /** Solo DESPACHO: vehiculo asignado a la entrega. */
    @Column(name = "vehiculo_placa", length = 15)
    private String vehiculoPlaca;

    /** Solo RECEPCION: control de calidad de la mercancia recibida. */
    @Column(name = "pesaje_kg", precision = 12, scale = 4)
    private BigDecimal pesajeKg;

    @Column(name = "humedad_pct", precision = 5, scale = 2)
    private BigDecimal humedadPct;

    @Column(name = "impureza_pct", precision = 5, scale = 2)
    private BigDecimal impurezaPct;

    /** Solo RECEPCION: resultado del control de calidad. Si es false, no se generan lotes. */
    @Column(name = "aceptado")
    private Boolean aceptado;

    @Column(name = "observaciones", length = 300)
    private String observaciones;

    @OneToMany(mappedBy = "movimiento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleMovimiento> detalles = new ArrayList<>();

    public void agregarDetalle(DetalleMovimiento detalle) {
        detalle.setMovimiento(this);
        this.detalles.add(detalle);
    }

    public enum TipoMovimiento {
        RECEPCION, DESPACHO, DEVOLUCION
    }

    public enum EstadoMovimiento {
        BORRADOR, CONFIRMADO, ANULADO
    }
}
