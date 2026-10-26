package pe.edu.upeu.bomerp.administracion.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.bomerp.administracion.model.Permiso;
import pe.edu.upeu.bomerp.administracion.model.Rol;
import pe.edu.upeu.bomerp.administracion.repository.PermisoRepository;
import pe.edu.upeu.bomerp.administracion.repository.RolRepository;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class RolService {

    private final RolRepository rolRepository;
    private final PermisoRepository permisoRepository;

    public Rol crear(Rol rol) {
        rol.setId(null);rol.setPermisos(new java.util.HashSet<>());return rolRepository.save(rol);
    }

    @Transactional(readOnly = true)
    public List<Rol> listar() {
        return rolRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Rol obtener(Long id) {
        return rolRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado: " + id));
    }

    public Rol asignarPermisos(Long rolId, Set<Long> permisoIds) {
        Rol rol = obtener(rolId);
        if(permisoIds==null)throw new IllegalArgumentException("Selecciona los permisos");var encontrados=permisoRepository.findAllById(permisoIds);if(encontrados.size()!=permisoIds.size())throw new IllegalArgumentException("Permiso inexistente");rol.setPermisos(new java.util.HashSet<>(encontrados));
        return rolRepository.save(rol);
    }
}
