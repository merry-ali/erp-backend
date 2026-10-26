package pe.edu.upeu.bomerp.personas.persona.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.bomerp.personas.persona.entity.Persona;
import pe.edu.upeu.bomerp.personas.persona.entity.RolPersona;

import java.util.List;

public interface PersonaRepository extends JpaRepository<Persona, Long> {
    boolean existsByNumeroDocumento(String numeroDocumento);
    
    boolean existsByNumeroDocumentoAndIdNot(String numeroDocumento, Long id);

    List<Persona> findByRolesContaining(RolPersona rol);
}
