package pe.edu.upeu.bomerp.administracion.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Punto de venta al publico dentro de una Sucursal (venta minorista presencial). */
@Entity
@Table(name = "tienda", schema = "bomerp")
@Getter
@Setter
@NoArgsConstructor
public class Tienda {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tienda_seq")
    @SequenceGenerator(name = "tienda_seq", sequenceName = "tienda_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sucursal_id", nullable = false)
    private Sucursal sucursal;

    @NotBlank
    @Column(name = "nombre", length = 150, nullable = false)
    private String nombre;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;
}
