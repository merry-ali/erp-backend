package pe.edu.upeu.bomerp.administracion.repository;

import pe.edu.upeu.bomerp.administracion.model.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
    boolean existsByRuc(String ruc);
}
