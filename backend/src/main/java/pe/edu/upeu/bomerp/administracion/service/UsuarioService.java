package pe.edu.upeu.bomerp.administracion.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.bomerp.administracion.model.Rol;
import pe.edu.upeu.bomerp.administracion.model.Usuario;
import pe.edu.upeu.bomerp.administracion.repository.UsuarioRepository;

/** Perfiles de negocio. Las credenciales se administran exclusivamente en Keycloak. */
@Service
@RequiredArgsConstructor
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final pe.edu.upeu.bomerp.personas.persona.repository.PersonaRepository personaRepository;
    private final RolService rolService;

    public java.util.List<Usuario> listar(){return usuarioRepository.findAll();}
    public Usuario crear(Long personaId, Long rolId, String username, String passwordPlano) {
        if (usuarioRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("El username '" + username + "' ya existe");
        }
        Usuario usuario = new Usuario();
        usuario.setPersona(personaRepository.findById(personaId).orElseThrow(() -> new IllegalArgumentException("Persona no encontrada")));
        usuario.setRol(rolService.obtener(rolId));
        usuario.setUsername(username);
        usuario.setPasswordHash(null); usuario.setKeycloakSub(passwordPlano); // TODO: reemplazar por hash (BCrypt) en produccion
        return usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public Usuario obtenerPorUsername(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + username));
    }

    @Transactional(readOnly = true)
    public Usuario obtener(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + id));
    }

    public Usuario cambiarRol(Long usuarioId, Long nuevoRolId) {
        Usuario usuario = obtener(usuarioId);
        usuario.setRol(rolService.obtener(nuevoRolId));
        return usuarioRepository.save(usuario);
    }
}
