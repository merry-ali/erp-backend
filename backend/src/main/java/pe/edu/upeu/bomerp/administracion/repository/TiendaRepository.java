package pe.edu.upeu.bomerp.administracion.repository;

import pe.edu.upeu.bomerp.administracion.model.Tienda;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TiendaRepository extends JpaRepository<Tienda, Long> {
    List<Tienda> findBySucursalId(Long sucursalId);
}
