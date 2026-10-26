package pe.edu.upeu.bomerp.personas.persona.service;

import pe.edu.upeu.bomerp.personas.persona.dto.PersonaRequest;
import pe.edu.upeu.bomerp.personas.persona.dto.PersonaResponse;
import pe.edu.upeu.bomerp.personas.persona.entity.RolPersona;

import java.util.List;

public interface PersonaService {
    List<PersonaResponse> listarPersonas(RolPersona rol);
    PersonaResponse obtenerPersona(Long id);
    PersonaResponse crearPersona(PersonaRequest request);
    PersonaResponse actualizarPersona(Long id, PersonaRequest request);
    void eliminarPersona(Long id);
    PersonaResponse obtenerProveedorActivo(Long id);
    boolean existeDocumento(String numeroDocumento, Long excluirId);
}
