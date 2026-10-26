package pe.edu.upeu.bomerp.administracion.repository;

import pe.edu.upeu.bomerp.administracion.model.Sucursal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SucursalRepository extends JpaRepository<Sucursal, Long> {
    List<Sucursal> findByEmpresaId(Long empresaId);
}
