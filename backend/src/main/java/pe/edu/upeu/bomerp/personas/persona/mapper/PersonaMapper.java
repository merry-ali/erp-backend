package pe.edu.upeu.bomerp.personas.persona.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import pe.edu.upeu.bomerp.personas.persona.dto.PersonaRequest;
import pe.edu.upeu.bomerp.personas.persona.dto.PersonaResponse;
import pe.edu.upeu.bomerp.personas.persona.entity.Persona;

@Mapper(componentModel = "spring")
public interface PersonaMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "activo", ignore = true) // boolean primitivo: nunca mapear un Boolean anulable directo, lo fija el service
    Persona toEntity(PersonaRequest request);

    PersonaResponse toResponse(Persona persona);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "activo", ignore = true) // idem: evita NPE por unboxing si el request no manda "activo"
    void updateEntityFromRequest(PersonaRequest request, @MappingTarget Persona persona);
}
