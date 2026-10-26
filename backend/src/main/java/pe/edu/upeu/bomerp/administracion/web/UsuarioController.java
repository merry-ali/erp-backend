package pe.edu.upeu.bomerp.administracion.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.bomerp.administracion.service.UsuarioService;
import pe.edu.upeu.bomerp.administracion.web.dto.UsuarioRequest;
import pe.edu.upeu.bomerp.administracion.web.dto.UsuarioResponse;

@org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/api/administracion/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse crear(@Valid @RequestBody UsuarioRequest request) {
        var usuario = usuarioService.crear(
                request.personaId(), request.rolId(), request.username(), request.keycloakSub());
        return UsuarioResponse.from(usuario);
    }

    @GetMapping public java.util.List<UsuarioResponse> listar(){return usuarioService.listar().stream().map(UsuarioResponse::from).toList();}
    @GetMapping("/{id}")
    public UsuarioResponse obtener(@PathVariable Long id) {
        return UsuarioResponse.from(usuarioService.obtener(id));
    }

    @PatchMapping("/{id}/rol/{nuevoRolId}")
    public UsuarioResponse cambiarRol(@PathVariable Long id, @PathVariable Long nuevoRolId) {
        return UsuarioResponse.from(usuarioService.cambiarRol(id, nuevoRolId));
    }
}
