package pe.edu.upeu.bomerp.produccion.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonBackReference;

/** Cantidad de un insumo/materia prima que la formula necesita por unidad de producto. */
@Entity
@Table(name = "formula_detalle", schema = "bomerp")
@Getter
@Setter
@NoArgsConstructor
public class FormulaDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "formula_detalle_seq")
    @SequenceGenerator(name = "formula_detalle_seq", sequenceName = "formula_detalle_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "formula_id", nullable = false)
    @JsonBackReference
    private Formula formula;
    
    /** Codigo de materia prima/insumo/material de empaque (referencia logica a Inventario). */
    @NotBlank
    @Column(name = "codigo_articulo", length = 30, nullable = false)
    private String codigoArticulo;

    @NotNull
    @Positive
    @Column(name = "cantidad_por_unidad", precision = 12, scale = 4, nullable = false)
    private BigDecimal cantidadPorUnidad;

    @NotBlank
    @Column(name = "unidad_medida", length = 15, nullable = false)
    private String unidadMedida;
}
