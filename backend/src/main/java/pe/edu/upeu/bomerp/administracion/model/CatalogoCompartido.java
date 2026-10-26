package pe.edu.upeu.bomerp.administracion.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Catalogo compartido generico: unidades de medida, categorias,
 * presentaciones, motivos de devolucion o de merma. Un solo modelo de
 * datos (tipo + codigo + descripcion) para no crear una tabla nueva cada
 * vez que otro modulo necesita una lista fija de valores.
 */
@Entity
@Table(name = "catalogo_compartido", schema = "bomerp",
        uniqueConstraints = @UniqueConstraint(columnNames = {"tipo", "codigo"}))
@Getter
@Setter
@NoArgsConstructor
public class CatalogoCompartido {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "catalogo_seq")
    @SequenceGenerator(name = "catalogo_seq", sequenceName = "catalogo_seq", allocationSize = 1)
    private Long id;

    /** ej: "UNIDAD_MEDIDA", "MOTIVO_MERMA", "MOTIVO_DEVOLUCION" */
    @NotBlank
    @Column(name = "tipo", length = 50, nullable = false)
    private String tipo;

    @NotBlank
    @Column(name = "codigo", length = 30, nullable = false)
    private String codigo;

    @NotBlank
    @Column(name = "descripcion", length = 150, nullable = false)
    private String descripcion;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;
}
