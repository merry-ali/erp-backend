package pe.edu.upeu.bomerp.articulo.web;
import org.springframework.web.bind.annotation.*;import org.springframework.security.access.prepost.PreAuthorize;import lombok.RequiredArgsConstructor;import jakarta.validation.Valid;import pe.edu.upeu.bomerp.articulo.model.Categoria;import pe.edu.upeu.bomerp.articulo.repository.CategoriaRepository;
@RestController @RequestMapping("/api/catalogo/categorias") @RequiredArgsConstructor public class CategoriaController {
private final CategoriaRepository repo;@GetMapping public java.util.List<Categoria> listar(){return repo.findAll();}@GetMapping("/{id}") public Categoria obtener(@PathVariable Long id){return repo.findById(id).orElseThrow(()->new IllegalArgumentException("Categoría no encontrada"));}
@PostMapping @PreAuthorize("@acceso.permite(authentication,'INVENTARIO')") public Categoria crear(@Valid @RequestBody Categoria c){c.setId(null);return repo.save(c);}
@PutMapping("/{id}") @PreAuthorize("@acceso.permite(authentication,'INVENTARIO')") public Categoria editar(@PathVariable Long id,@Valid @RequestBody Categoria c){var e=obtener(id);e.setNombre(c.getNombre());return repo.save(e);}
@DeleteMapping("/{id}") @PreAuthorize("@acceso.permite(authentication,'INVENTARIO')") public void eliminar(@PathVariable Long id){repo.deleteById(id);}}
