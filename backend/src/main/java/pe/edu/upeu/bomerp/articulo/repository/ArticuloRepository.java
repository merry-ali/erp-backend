package pe.edu.upeu.bomerp.articulo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.bomerp.articulo.model.Articulo;

import java.util.List;
import java.util.Optional;

public interface ArticuloRepository extends JpaRepository<Articulo, Long> {
    Optional<Articulo> findByCodigo(String codigo);
    List<Articulo> findByActivoTrue();
    List<Articulo> findByTipoArticuloAndActivoTrue(Articulo.TipoArticulo tipoArticulo);
}
