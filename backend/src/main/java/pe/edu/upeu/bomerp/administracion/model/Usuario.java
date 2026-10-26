package pe.edu.upeu.bomerp.administracion.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Usuario que inicia sesion en el Portal Web -- es un rol sobre
 * {@link Persona}, igual que Proveedor y Cliente. Si el dia de manana se
 * delega la autenticacion a un Identity Provider externo (ej. Keycloak),
 * esta tabla deja de guardar {@code passwordHash} pero sigue siendo
 * duena del {@link Rol} y de a que modulos da acceso.
 */
@Entity
@Table(name = "usuario", schema = "bomerp")
@Getter
@Setter
@NoArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "usuario_seq")
    @SequenceGenerator(name = "usuario_seq", sequenceName = "usuario_seq", allocationSize = 1)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "persona_id", nullable = false, unique = true)
    private pe.edu.upeu.bomerp.personas.persona.entity.Persona persona;

    @NotBlank
    @Column(name = "username", length = 60, nullable = false, unique = true)
    private String username;

    /** Null si la autenticacion se delega a un Identity Provider externo. */
    @Column(name = "password_hash", length = 255)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private String passwordHash;
    @Column(unique = true)
    private String keycloakSub;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rol_id", nullable = false)
    private Rol rol;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;
}
