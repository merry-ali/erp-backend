package pe.edu.upeu.bomerp.produccion.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonManagedReference;

/** Formula/receta de un producto terminado (ej. una presentacion de purina). */
@Entity
@Table(name = "formula", schema = "bomerp")
@Getter
@Setter
@NoArgsConstructor
public class Formula {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "formula_seq")
    @SequenceGenerator(name = "formula_seq", sequenceName = "formula_seq", allocationSize = 1)
    private Long id;

    /** Codigo del producto terminado que esta formula produce (referencia logica a Inventario/Ventas). */
    @NotBlank
    @Column(name = "codigo_producto_terminado", length = 30, nullable = false)
    private String codigoProductoTerminado;

    @NotBlank
    @Column(name = "nombre", length = 150, nullable = false)
    private String nombre;

    @Column(name = "version", nullable = false)
    private Integer version = 1;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    @JsonManagedReference
    @OneToMany(mappedBy = "formula", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FormulaDetalle> detalles = new ArrayList<>();

    public void agregarDetalle(FormulaDetalle detalle) {
        detalle.setFormula(this);
        this.detalles.add(detalle);
    }
}
