package pe.edu.upeu.bomerp.administracion.web.dto;

import java.util.Set;

public record AsignarPermisosRequest(Set<Long> permisoIds) {
}
