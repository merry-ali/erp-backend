package pe.edu.upeu.bomerp.inventario.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.bomerp.inventario.model.MovimientoInventario;

import java.util.List;

public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {
    List<MovimientoInventario> findByTipoMovimiento(MovimientoInventario.TipoMovimiento tipoMovimiento);
    List<MovimientoInventario> findByEstado(MovimientoInventario.EstadoMovimiento estado);
    List<MovimientoInventario> findByOrdenCompraId(Long ordenCompraId);
    List<MovimientoInventario> findByPedidoVentaId(Long pedidoVentaId);
}
