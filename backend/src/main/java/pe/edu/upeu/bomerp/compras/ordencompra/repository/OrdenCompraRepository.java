package pe.edu.upeu.bomerp.compras.ordencompra.repository;
import java.util.*;import java.time.LocalDateTime;import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;import org.springframework.data.repository.query.Param;
import pe.edu.upeu.bomerp.compras.ordencompra.entity.*;
public interface OrdenCompraRepository extends JpaRepository<OrdenCompra,Long> {
 @EntityGraph(attributePaths="detalles") Optional<OrdenCompra> findWithDetallesById(Long id);
 @EntityGraph(attributePaths="detalles")
 @Query("select o from OrdenCompra o where (:estado is null or o.estado=:estado) and (:proveedorId is null or o.proveedorId=:proveedorId) and (:inicio is null or o.fecha>=:inicio) and (:fin is null or o.fecha<=:fin) and (:operadorId is null or o.operadorId=:operadorId) order by o.fecha desc,o.id desc")
 List<OrdenCompra> buscar(@Param("estado")EstadoOrdenCompra estado,@Param("proveedorId")Long proveedorId,@Param("inicio")LocalDateTime inicio,@Param("fin")LocalDateTime fin,@Param("operadorId")String operadorId);
 @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select o from OrdenCompra o where o.id=:id")Optional<OrdenCompra> bloquear(@Param("id")Long id);
}
