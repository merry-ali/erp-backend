package pe.edu.upeu.bomerp.articulo.web;
import org.springframework.web.bind.annotation.*;import lombok.RequiredArgsConstructor;import jakarta.validation.Valid;import pe.edu.upeu.bomerp.articulo.model.UnidadMedida;import pe.edu.upeu.bomerp.articulo.repository.UnidadMedidaRepository;
@RestController @RequiredArgsConstructor @RequestMapping("/api/articulos/unidades-medida") @org.springframework.security.access.prepost.PreAuthorize("@acceso.permite(authentication,'INVENTARIO')") public class UnidadCrud {
private final UnidadMedidaRepository repo; @PutMapping("/{id}") public UnidadMedida editar(@PathVariable Long id,@Valid @RequestBody UnidadMedida r){var e=repo.findById(id).orElseThrow();e.setNombre(r.getNombre());e.setSimbolo(r.getSimbolo());return repo.save(e);}
@DeleteMapping("/{id}") public void eliminar(@PathVariable Long id){repo.deleteById(id);}}
