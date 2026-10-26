package pe.edu.upeu.bomerp.administracion.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Registro de auditoria de lo que los usuarios hacen en el resto de modulos. */
@Entity
@Table(name = "auditoria", schema = "bomerp")
@Getter
@Setter
@NoArgsConstructor
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "auditoria_seq")
    @SequenceGenerator(name = "auditoria_seq", sequenceName = "auditoria_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "modulo", length = 30, nullable = false)
    private Permiso.Modulo modulo;

    @Column(name = "entidad", length = 100, nullable = false)
    private String entidad;

    @Column(name = "accion", length = 30, nullable = false)
    private String accion; // CREAR, ACTUALIZAR, ELIMINAR, APROBAR, etc.

    @Column(name = "detalle", length = 1000)
    private String detalle;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();
}
