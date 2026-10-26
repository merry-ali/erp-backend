package pe.edu.upeu.bomerp.produccion.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Consumo real de un insumo/materia prima al ejecutar una orden. Puede
 * diferir de lo planificado en {@link pe.edu.upeu.bomerp.produccion.model.FormulaDetalle}
 * (mermas de manipulacion, ajustes de calidad, etc.). El {@code loteId}
 * es una referencia logica al lote que Inventario asigno en la recepcion.
 */
@Entity
@Table(name = "consumo_insumo", schema = "bomerp")
@Getter
@Setter
@NoArgsConstructor
public class ConsumoInsumo {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "consumo_insumo_seq")
    @SequenceGenerator(name = "consumo_insumo_seq", sequenceName = "consumo_insumo_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "orden_produccion_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private OrdenProduccion ordenProduccion;

    @NotBlank
    @Column(name = "codigo_articulo", length = 30, nullable = false)
    private String codigoArticulo;

    /** Referencia logica al lote de Inventario consumido (trazabilidad hacia atras). */
    @Column(name = "lote_id")
    private Long loteId;

    @NotNull
    @Positive
    @Column(name = "cantidad_consumida", precision = 12, scale = 4, nullable = false)
    private BigDecimal cantidadConsumida;

    @NotBlank
    @Column(name = "unidad_medida", length = 15, nullable = false)
    private String unidadMedida;
}
