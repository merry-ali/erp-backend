package pe.edu.upeu.bomerp.personas.persona.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.bomerp.exception.DocumentoDuplicadoException;
import pe.edu.upeu.bomerp.exception.ProveedorInvalidoException;
import pe.edu.upeu.bomerp.exception.ResourceNotFoundException;
import pe.edu.upeu.bomerp.personas.persona.dto.PersonaRequest;
import pe.edu.upeu.bomerp.personas.persona.dto.PersonaResponse;
import pe.edu.upeu.bomerp.personas.persona.entity.Persona;
import pe.edu.upeu.bomerp.personas.persona.entity.RolPersona;
import pe.edu.upeu.bomerp.personas.persona.mapper.PersonaMapper;
import pe.edu.upeu.bomerp.personas.persona.repository.PersonaRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PersonaServiceImpl implements PersonaService {

    private final PersonaRepository personaRepository;
    private final PersonaMapper personaMapper;

    @Override
    @Transactional(readOnly = true)
    public List<PersonaResponse> listarPersonas(RolPersona rol) {
        List<Persona> personas;
        if (rol != null) {
            personas = personaRepository.findByRolesContaining(rol);
        } else {
            personas = personaRepository.findAll();
        }
        return personas.stream()
                .map(personaMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PersonaResponse obtenerPersona(Long id) {
        Persona persona = buscarPersonaPorId(id);
        return personaMapper.toResponse(persona);
    }

    @Override
    @Transactional
    public PersonaResponse crearPersona(PersonaRequest request) {
        if (personaRepository.existsByNumeroDocumento(request.numeroDocumento())) {
            throw new DocumentoDuplicadoException("Ya existe una persona con el número de documento " + request.numeroDocumento());
        }

        Persona persona = personaMapper.toEntity(request);
        if (request.activo() != null) {
            persona.setActivo(request.activo());
        } else {
            persona.setActivo(true);
        }
        
        Persona personaGuardada = personaRepository.save(persona);
        return personaMapper.toResponse(personaGuardada);
    }

    @Override
    @Transactional
    public PersonaResponse actualizarPersona(Long id, PersonaRequest request) {
        Persona persona = buscarPersonaPorId(id);

        if (personaRepository.existsByNumeroDocumentoAndIdNot(request.numeroDocumento(), id)) {
            throw new DocumentoDuplicadoException("El número de documento " + request.numeroDocumento() + " ya está en uso por otra persona");
        }

        personaMapper.updateEntityFromRequest(request, persona);
        if (request.activo() != null) {
            persona.setActivo(request.activo());
        }

        Persona personaActualizada = personaRepository.save(persona);
        return personaMapper.toResponse(personaActualizada);
    }

    @Override
    @Transactional
    public void eliminarPersona(Long id) {
        Persona persona = buscarPersonaPorId(id);
        persona.setActivo(false);
        personaRepository.save(persona);
    }

    @Override
    @Transactional(readOnly = true)
    public PersonaResponse obtenerProveedorActivo(Long id) {
        Persona persona = buscarPersonaPorId(id);
        if (!persona.isActivo()) {
            throw new ProveedorInvalidoException("El proveedor con ID " + id + " no está activo");
        }
        if (!persona.getRoles().contains(RolPersona.PROVEEDOR)) {
            throw new ProveedorInvalidoException("La persona con ID " + id + " no tiene el rol de PROVEEDOR");
        }
        return personaMapper.toResponse(persona);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existeDocumento(String numeroDocumento, Long excluirId) {
        String numero = numeroDocumento == null ? "" : numeroDocumento.trim();
        return excluirId == null
                ? personaRepository.existsByNumeroDocumento(numero)
                : personaRepository.existsByNumeroDocumentoAndIdNot(numero, excluirId);
    }

    private Persona buscarPersonaPorId(Long id) {
        return personaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Persona no encontrada con id: " + id));
    }
}
