package pe.edu.upeu.bomerp.produccion.repository;

import pe.edu.upeu.bomerp.produccion.model.OrdenProduccion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrdenProduccionRepository extends JpaRepository<OrdenProduccion, Long> {
    List<OrdenProduccion> findByEstado(OrdenProduccion.EstadoOrden estado);
    List<OrdenProduccion> findByFormulaId(Long formulaId);
}
