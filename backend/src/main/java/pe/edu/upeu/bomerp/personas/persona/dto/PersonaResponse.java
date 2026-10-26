package pe.edu.upeu.bomerp.personas.persona.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.edu.upeu.bomerp.personas.persona.entity.TipoDocumento;
import pe.edu.upeu.bomerp.personas.persona.entity.RolPersona;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonaResponse {
    private Long id;
    private TipoDocumento tipoDocumento;
    private String numeroDocumento;
    private String nombreORazonSocial;
    private String direccion;
    private String telefono;
    private String email;
    private Set<RolPersona> roles;
    private boolean activo;
}
