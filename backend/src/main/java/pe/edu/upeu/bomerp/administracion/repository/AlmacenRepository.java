package pe.edu.upeu.bomerp.administracion.repository;

import pe.edu.upeu.bomerp.administracion.model.Almacen;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlmacenRepository extends JpaRepository<Almacen, Long> {
    List<Almacen> findBySucursalId(Long sucursalId);
    List<Almacen> findByTipo(Almacen.TipoAlmacen tipo);
}
