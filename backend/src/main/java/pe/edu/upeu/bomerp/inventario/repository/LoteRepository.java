package pe.edu.upeu.bomerp.inventario.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.bomerp.inventario.model.Lote;

import java.util.List;

public interface LoteRepository extends JpaRepository<Lote, Long> {
 @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
 @Query("select l from Lote l where l.codigoArticulo=:codigo and l.almacenId=:almacen and l.estado=pe.edu.upeu.bomerp.inventario.model.Lote.EstadoLote.DISPONIBLE order by l.id")
 List<Lote> bloquearDisponibles(@Param("codigo") String codigo,@Param("almacen") Long almacen);


    List<Lote> findByCodigoArticuloAndAlmacenIdAndEstado(
            String codigoArticulo, Long almacenId, Lote.EstadoLote estado);

    @Query("select coalesce(sum(l.cantidadDisponible), 0) from Lote l " +
            "where l.codigoArticulo = :codigoArticulo and l.almacenId = :almacenId " +
            "and l.estado = pe.edu.upeu.bomerp.inventario.model.Lote.EstadoLote.DISPONIBLE")
    java.math.BigDecimal sumarDisponible(@Param("codigoArticulo") String codigoArticulo,
                                          @Param("almacenId") Long almacenId);
}
