package pe.edu.upeu.bomerp.administracion.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Permiso sobre un modulo del sistema (ej. acceso a "PRODUCCION",
 * "FINANZAS"). El menu que el Portal Web le muestra a un usuario con
 * sesion se arma preguntando aqui, nunca decidiendo por su cuenta.
 */
@Entity
@Table(name = "permiso", schema = "bomerp")
@Getter
@Setter
@NoArgsConstructor
public class Permiso {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "permiso_seq")
    @SequenceGenerator(name = "permiso_seq", sequenceName = "permiso_seq", allocationSize = 1)
    private Long id;

    @NotBlank
    @Column(name = "nombre", length = 80, nullable = false, unique = true)
    private String nombre; // ej: "VER_PRODUCCION", "OPERAR_PRODUCCION"

    @Enumerated(EnumType.STRING)
    @Column(name = "modulo", length = 30, nullable = false)
    private Modulo modulo;

    @Column(name = "descripcion", length = 250)
    private String descripcion;

    public enum Modulo {
        COMPRAS, INVENTARIO, PRODUCCION, VENTAS, FINANZAS, ADMINISTRACION
    }
}
