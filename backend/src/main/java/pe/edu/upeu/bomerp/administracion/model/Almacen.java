package pe.edu.upeu.bomerp.administracion.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Espacio fisico de existencias dentro de una Sucursal. Inventario y
 * Produccion lo referencian solo por {@code id} (no con una relacion JPA
 * cruzando de modulo), para no acoplar el modelo de datos entre modulos.
 */
@Entity
@Table(name = "almacen", schema = "bomerp")
@Getter
@Setter
@NoArgsConstructor
public class Almacen {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "almacen_seq")
    @SequenceGenerator(name = "almacen_seq", sequenceName = "almacen_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sucursal_id", nullable = false)
    private Sucursal sucursal;

    @NotBlank
    @Column(name = "nombre", length = 150, nullable = false)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", length = 30, nullable = false)
    private TipoAlmacen tipo;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    public enum TipoAlmacen {
        MATERIA_PRIMA, PRODUCTO_TERMINADO, INSUMOS, EMPAQUE, GENERAL
    }
}
