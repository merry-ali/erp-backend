package pe.edu.upeu.bomerp.articulo.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.bomerp.articulo.model.Articulo;
import pe.edu.upeu.bomerp.articulo.service.ArticuloService;
import pe.edu.upeu.bomerp.articulo.web.dto.ArticuloPublicoResponse;

import java.util.List;

@org.springframework.security.access.prepost.PreAuthorize("@acceso.permite(authentication, 'INVENTARIO')")
@RestController
@RequestMapping("/api/articulos")
@RequiredArgsConstructor
public class ArticuloController {

    private final ArticuloService articuloService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Articulo crear(@Valid @RequestBody Articulo articulo) {
        return articuloService.crear(articulo);
    }

    @org.springframework.security.access.prepost.PreAuthorize("isAuthenticated()")
    @GetMapping
    public List<Articulo> listar(@RequestParam(required = false) Articulo.TipoArticulo tipo) {
        return tipo == null ? articuloService.listar() : articuloService.listarPorTipo(tipo);
    }

    @org.springframework.security.access.prepost.PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public Articulo obtener(@PathVariable Long id) {
        return articuloService.obtener(id);
    }

    @org.springframework.security.access.prepost.PreAuthorize("isAuthenticated()")
    @GetMapping("/codigo/{codigo}")
    public Articulo obtenerPorCodigo(@PathVariable String codigo) {
        return articuloService.obtenerPorCodigo(codigo);
    }

    /** Consumido por el Portal Web para el catalogo de visitantes anonimos. Sin costo ni stock. */
    @org.springframework.security.access.prepost.PreAuthorize("permitAll()")
    @GetMapping("/catalogo-publico")
    public List<ArticuloPublicoResponse> catalogoPublico() {
        return articuloService.catalogoPublico();
    }

    @PutMapping("/{id}")
    public Articulo actualizar(@PathVariable Long id, @Valid @RequestBody Articulo articulo) {
        return articuloService.actualizar(id, articulo);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id){articuloService.desactivar(id);}
    @PatchMapping("/{id}/desactivar")
    public Articulo desactivar(@PathVariable Long id) {
        return articuloService.desactivar(id);
    }
}
