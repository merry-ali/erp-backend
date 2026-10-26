package pe.edu.upeu.bomerp.administracion.web;
import pe.edu.upeu.bomerp.administracion.model.*;import pe.edu.upeu.bomerp.administracion.repository.*;import org.springframework.web.bind.annotation.*;import org.springframework.transaction.annotation.Transactional;import org.springframework.security.access.prepost.PreAuthorize;import jakarta.validation.Valid;import lombok.RequiredArgsConstructor;import java.util.*;
@RestController @RequestMapping("/api/gestion/roles") @RequiredArgsConstructor @Transactional public class RolCrud {
 private final RolRepository repo;
 @GetMapping public List<Rol> listar(){return repo.findAll();}
 @GetMapping("/{id}") public Rol obtener(@PathVariable Long id){return repo.findById(id).orElseThrow(()->new IllegalArgumentException("Registro no encontrado"));}
 @PostMapping @PreAuthorize("hasRole('ADMIN')") public Rol crear(@Valid @RequestBody Rol r){var e=new Rol();e.setNombre(r.getNombre());e.setDescripcion(r.getDescripcion());return repo.save(e);}
 @PutMapping("/{id}") @PreAuthorize("hasRole('ADMIN')") public Rol actualizar(@PathVariable Long id,@Valid @RequestBody Rol r){var e=obtener(id);e.setNombre(r.getNombre());e.setDescripcion(r.getDescripcion());return repo.save(e);}
 @DeleteMapping("/{id}") @PreAuthorize("hasRole('ADMIN')") public void eliminar(@PathVariable Long id){repo.delete(obtener(id));repo.flush();}
}