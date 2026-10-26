package pe.edu.upeu.bomerp.produccion.repository;

import pe.edu.upeu.bomerp.produccion.model.Formula;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FormulaRepository extends JpaRepository<Formula, Long> {
    List<Formula> findByCodigoProductoTerminadoAndActivoTrue(String codigoProductoTerminado);
}
