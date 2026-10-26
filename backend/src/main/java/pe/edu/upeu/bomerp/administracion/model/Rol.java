package pe.edu.upeu.bomerp.administracion.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * Rol de negocio (ej. COMPRADOR, ALMACENERO, OPERARIO_PLANTA, VENDEDOR,
 * CAJERO, ADMIN). El Portal Web pregunta a Administracion por el rol del
 * usuario para armar su menu -- el rol vive aqui, el Portal solo lo refleja.
 */
@Entity
@Table(name = "rol", schema = "bomerp")
@Getter
@Setter
@NoArgsConstructor
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "rol_seq")
    @SequenceGenerator(name = "rol_seq", sequenceName = "rol_seq", allocationSize = 1)
    private Long id;

    @NotBlank
    @Column(name = "nombre", length = 80, nullable = false, unique = true)
    private String nombre;

    @Column(name = "descripcion", length = 250)
    private String descripcion;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "rol_permiso",
            schema = "bomerp",
            joinColumns = @JoinColumn(name = "rol_id"),
            inverseJoinColumns = @JoinColumn(name = "permiso_id")
    )
    private Set<Permiso> permisos = new HashSet<>();

    @Column(name = "activo", nullable = false)
    private boolean activo = true;
}
