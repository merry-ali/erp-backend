package pe.edu.upeu.bomerp.articulo.model;
@jakarta.persistence.Entity @jakarta.persistence.Table(name="categoria",schema="bomerp") @lombok.Getter @lombok.Setter public class Categoria {
@jakarta.persistence.Id @jakarta.persistence.GeneratedValue(strategy=jakarta.persistence.GenerationType.SEQUENCE) private Long id;
@jakarta.validation.constraints.NotBlank @jakarta.persistence.Column(unique=true,nullable=false) private String nombre;
private boolean activo=true;}
