package pe.edu.upeu.bomerp.personas.persona.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "PERSONAS", schema = "bomerp")
@Getter
@Setter
@NoArgsConstructor
public class Persona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "TIPO_DOCUMENTO", nullable = false, length = 20)
    private TipoDocumento tipoDocumento;

    @Column(name = "NUMERO_DOCUMENTO", nullable = false, unique = true, length = 20)
    private String numeroDocumento;

    @Column(name = "NOMBRE_RAZON_SOCIAL", nullable = false, length = 150)
    private String nombreORazonSocial;

    @Column(name = "DIRECCION", length = 200)
    private String direccion;

    @Column(name = "TELEFONO", length = 20)
    private String telefono;

    @Column(name = "EMAIL", length = 120)
    private String email;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "PERSONA_ROLES",
            schema = "bomerp",
            joinColumns = @JoinColumn(name = "ID_PERSONA")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "ROL", nullable = false, length = 20)
    private Set<RolPersona> roles = new HashSet<>();

    @Column(name = "ACTIVO", nullable = false)
    private boolean activo = true;
}
