package pe.edu.upeu.bomerp.produccion.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Lo que resulta de ejecutar una orden de produccion: producto terminado,
 * subproducto, merma, desperdicio o reproceso. Solo TERMINADO y
 * SUBPRODUCTO generan lote y entran a existencias de Inventario (via el
 * puerto de integracion); MERMA/DESPERDICIO/REPROCESO se registran para
 * trazabilidad y calculo de rendimiento, sin generar entrada de stock.
 */
@Entity
@Table(name = "producto_resultante", schema = "bomerp")
@Getter
@Setter
@NoArgsConstructor
public class ProductoResultante {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "producto_resultante_seq")
    @SequenceGenerator(name = "producto_resultante_seq", sequenceName = "producto_resultante_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "orden_produccion_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private OrdenProduccion ordenProduccion;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", length = 20, nullable = false)
    private TipoResultado tipo;

    @NotBlank
    @Column(name = "codigo_articulo", length = 30, nullable = false)
    private String codigoArticulo;

    @NotNull
    @Column(name = "cantidad", precision = 12, scale = 4, nullable = false)
    private BigDecimal cantidad;

    @NotBlank
    @Column(name = "unidad_medida", length = 15, nullable = false)
    private String unidadMedida;

    /** Lote generado en Inventario para TERMINADO/SUBPRODUCTO (null para merma/desperdicio/reproceso). */
    @Column(name = "lote_generado_id")
    private Long loteGeneradoId;

    public enum TipoResultado {
        TERMINADO, SUBPRODUCTO, MERMA, DESPERDICIO, REPROCESO
    }
}
