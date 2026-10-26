package pe.edu.upeu.bomerp.administracion.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.bomerp.administracion.model.Auditoria;
import pe.edu.upeu.bomerp.administracion.model.Permiso;
import pe.edu.upeu.bomerp.administracion.model.Usuario;
import pe.edu.upeu.bomerp.administracion.repository.AuditoriaRepository;

import java.util.List;

/**
 * Registro de auditoria de acciones en los demas modulos. Otros modulos
 * (ej. Produccion) llaman a {@code registrar} tras una accion relevante
 * (cerrar una orden de produccion, aprobar un pedido, etc.).
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;

    public void registrar(Usuario usuario, Permiso.Modulo modulo, String entidad, String accion, String detalle) {
        Auditoria auditoria = new Auditoria();
        auditoria.setUsuario(usuario);
        auditoria.setModulo(modulo);
        auditoria.setEntidad(entidad);
        auditoria.setAccion(accion);
        auditoria.setDetalle(detalle);
        auditoriaRepository.save(auditoria);
    }

    @Transactional(readOnly = true)
    public List<Auditoria> historialDeUsuario(Long usuarioId) {
        return auditoriaRepository.findByUsuarioIdOrderByFechaDesc(usuarioId);
    }
}
