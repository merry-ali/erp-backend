package pe.edu.upeu.bomerp.portal;
@jakarta.persistence.Entity @jakarta.persistence.Table(name="solicitud_contacto",schema="bomerp") @lombok.Getter @lombok.Setter public class Contacto {
@jakarta.persistence.Id @jakarta.persistence.GeneratedValue(strategy=jakarta.persistence.GenerationType.SEQUENCE) private Long id;
@jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Size(max=120) private String nombre;
@jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Email private String email;
@jakarta.validation.constraints.NotBlank @jakarta.validation.constraints.Size(max=1000) @jakarta.persistence.Column(length=1000) private String mensaje;
private java.time.LocalDateTime fecha=java.time.LocalDateTime.now();
}