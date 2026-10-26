package pe.edu.upeu.bomerp.administracion.web.dto;

import pe.edu.upeu.bomerp.administracion.model.Usuario;

/** Nunca exponer passwordHash en las respuestas. */
public record UsuarioResponse(Long id, String username, String rol, String persona, boolean activo) {

    public static UsuarioResponse from(Usuario u) {
        return new UsuarioResponse(
                u.getId(), u.getUsername(), u.getRol().getNombre(),
                u.getPersona().getNombreORazonSocial(), u.isActivo());
    }
}
