package pe.edu.upeu.bomerp.inventario.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Linea de un movimiento: cuanto de que articulo entra o sale.
 * <p>
 * En RECEPCION, {@code loteId} queda null hasta que la recepcion se
 * confirma como aceptada -- en ese momento se genera el {@link Lote} y se
 * enlaza aqui, para trazabilidad hacia adelante. En DESPACHO,
 * {@code loteId} se especifica de entrada: es la seleccion de lote que
 * exige el requisito ("especificando seleccion de lotes").
 */
@Entity
@Table(name = "detalle_movimiento", schema = "bomerp")
@Getter
@Setter
@NoArgsConstructor
public class DetalleMovimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "detalle_movimiento_seq")
    @SequenceGenerator(name = "detalle_movimiento_seq", sequenceName = "detalle_movimiento_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "movimiento_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private MovimientoInventario movimiento;

    /** Referencia logica al modulo Catalogo de Articulos. */
    @NotBlank
    @Column(name = "codigo_articulo", length = 30, nullable = false)
    private String codigoArticulo;

    /** DESPACHO: lote elegido de antemano. RECEPCION: se completa al confirmar (si es aceptada). */
    @Column(name = "lote_id")
    private Long loteId;

    @NotNull
    @Positive
    @Column(name = "cantidad", precision = 12, scale = 4, nullable = false)
    private BigDecimal cantidad;

    @NotBlank
    @Column(name = "unidad_medida", length = 15, nullable = false)
    private String unidadMedida;
}
