package pe.edu.upeu.bomerp.administracion.repository;

import pe.edu.upeu.bomerp.administracion.model.CatalogoCompartido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CatalogoCompartidoRepository extends JpaRepository<CatalogoCompartido, Long> {
    List<CatalogoCompartido> findByTipoAndActivoTrue(String tipo);
}
