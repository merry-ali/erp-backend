package pe.edu.upeu.bomerp.administracion.web;
import pe.edu.upeu.bomerp.administracion.model.*;import pe.edu.upeu.bomerp.administracion.repository.*;import org.springframework.web.bind.annotation.*;import org.springframework.transaction.annotation.Transactional;import org.springframework.security.access.prepost.PreAuthorize;import jakarta.validation.Valid;import lombok.RequiredArgsConstructor;import java.util.*;
@RestController @RequestMapping("/api/gestion/empresas") @RequiredArgsConstructor @Transactional public class EmpresaCrud {
 private final EmpresaRepository repo;
 @GetMapping public List<Empresa> listar(){return repo.findAll();}
 @GetMapping("/{id}") public Empresa obtener(@PathVariable Long id){return repo.findById(id).orElseThrow(()->new IllegalArgumentException("Registro no encontrado"));}
 @PostMapping @PreAuthorize("hasRole('ADMIN')") public Empresa crear(@Valid @RequestBody Empresa r){var e=new Empresa();e.setRuc(r.getRuc());e.setRazonSocial(r.getRazonSocial());e.setNombreComercial(r.getNombreComercial());return repo.save(e);}
 @PutMapping("/{id}") @PreAuthorize("hasRole('ADMIN')") public Empresa actualizar(@PathVariable Long id,@Valid @RequestBody Empresa r){var e=obtener(id);e.setRuc(r.getRuc());e.setRazonSocial(r.getRazonSocial());e.setNombreComercial(r.getNombreComercial());return repo.save(e);}
 @DeleteMapping("/{id}") @PreAuthorize("hasRole('ADMIN')") public void eliminar(@PathVariable Long id){repo.delete(obtener(id));repo.flush();}
}