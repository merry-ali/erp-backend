package pe.edu.upeu.bomerp.articulo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.bomerp.articulo.model.UnidadMedida;

import java.util.List;
import java.util.Optional;

public interface UnidadMedidaRepository extends JpaRepository<UnidadMedida, Long> {
    Optional<UnidadMedida> findByCodigo(String codigo);
    List<UnidadMedida> findByActivoTrue();
}
