package pe.edu.upeu.bomerp.articulo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Unidad de medida centralizada (kg, litro, saco, unidad, etc.). Todos
 * los demas modulos que operan cantidades (Inventario, Produccion,
 * Compras, Ventas) referencian el {@code codigo} de esta tabla en vez de
 * escribir el texto suelto cada vez, para evitar que "kg", "KG" y
 * "Kilogramo" convivan como si fueran unidades distintas.
 */
@Entity
@Table(name = "unidad_medida", schema = "bomerp",
        uniqueConstraints = @UniqueConstraint(columnNames = "codigo"))
@Getter
@Setter
@NoArgsConstructor
public class UnidadMedida {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "unidad_medida_seq")
    @SequenceGenerator(name = "unidad_medida_seq", sequenceName = "unidad_medida_seq", allocationSize = 1)
    private Long id;

    /** ej: "KG", "LT", "SACO", "UND" */
    @NotBlank
    @Column(name = "codigo", length = 10, nullable = false)
    private String codigo;

    @NotBlank
    @Column(name = "nombre", length = 50, nullable = false)
    private String nombre;

    @Column(name = "simbolo", length = 10)
    private String simbolo;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;
}
