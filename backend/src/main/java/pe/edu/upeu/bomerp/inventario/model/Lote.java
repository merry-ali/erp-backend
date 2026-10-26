package pe.edu.upeu.bomerp.inventario.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Existencia real de un articulo en un almacen. Un lote nace SOLO cuando
 * una recepcion es aceptada en control de calidad (nunca de forma
 * optimista antes de confirmar) y se va descontando a medida que se
 * despacha o que Produccion consume materia prima. La cantidad
 * disponible que Ventas/Produccion consultan en tiempo real es la suma
 * de {@code cantidadDisponible} de los lotes activos de un articulo en
 * un almacen.
 */
@Entity
@Table(name = "lote", schema = "bomerp")
@Getter
@Setter
@NoArgsConstructor
public class Lote {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "lote_seq")
    @SequenceGenerator(name = "lote_seq", sequenceName = "lote_seq", allocationSize = 1)
    private Long id;
    @Version
    private Long revision;

    /** Referencia logica al modulo Catalogo de Articulos (nunca relacion JPA directa). */
    @NotBlank
    @Column(name = "codigo_articulo", length = 30, nullable = false)
    private String codigoArticulo;

    /** Referencia logica al almacen de Administracion. */
    @NotNull
    @Column(name = "almacen_id", nullable = false)
    private Long almacenId;

    @NotNull
    @Column(name = "cantidad_inicial", precision = 12, scale = 4, nullable = false)
    private BigDecimal cantidadInicial;

    @NotNull
    @Column(name = "cantidad_disponible", precision = 12, scale = 4, nullable = false)
    private BigDecimal cantidadDisponible;

    /** Movimiento de RECEPCION que dio origen a este lote (trazabilidad hacia atras). */
    @Column(name = "movimiento_origen_id")
    private Long movimientoOrigenId;

    @Column(name = "fecha_ingreso", nullable = false)
    private LocalDateTime fechaIngreso = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 15, nullable = false)
    private EstadoLote estado = EstadoLote.DISPONIBLE;

    public void descontar(BigDecimal cantidad) {
        BigDecimal restante = this.cantidadDisponible.subtract(cantidad);
        if (restante.signum() < 0) {
            throw new IllegalStateException("El lote " + id + " no tiene suficiente cantidad disponible");
        }
        this.cantidadDisponible = restante;
        if (restante.signum() == 0) {
            this.estado = EstadoLote.AGOTADO;
        }
    }

    public void reingresar(BigDecimal cantidad) {
        if (cantidad == null || cantidad.signum() <= 0) {
            throw new IllegalArgumentException("La cantidad a devolver debe ser positiva");
        }
        this.cantidadDisponible = this.cantidadDisponible.add(cantidad);
        this.estado = EstadoLote.DISPONIBLE;
    }

    public enum EstadoLote {
        DISPONIBLE, AGOTADO, BLOQUEADO
    }
}
