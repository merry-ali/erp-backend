package pe.edu.upeu.bomerp.articulo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.bomerp.articulo.model.Articulo;
import pe.edu.upeu.bomerp.articulo.repository.ArticuloRepository;
import pe.edu.upeu.bomerp.articulo.web.dto.ArticuloPublicoResponse;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ArticuloService {

    private final ArticuloRepository articuloRepository;
    private final UnidadMedidaService unidades;
    private final pe.edu.upeu.bomerp.articulo.repository.CategoriaRepository categorias;
    private void validar(Articulo a){if(a.getCategoria()!=null){if(a.getCategoria().getId()==null)throw new IllegalArgumentException("Selecciona una categoría válida");a.setCategoria(categorias.findById(a.getCategoria().getId()).filter(c->c.isActivo()).orElseThrow(()->new IllegalArgumentException("Categoría no existe o está inactiva")));}if(a.getUnidadMedida()==null||a.getUnidadMedida().getId()==null)throw new IllegalArgumentException("Selecciona una unidad de medida");var u=unidades.obtener(a.getUnidadMedida().getId());if(!u.isActivo())throw new IllegalArgumentException("Unidad inactiva");a.setUnidadMedida(u);if(a.getCostoReferencial()==null||a.getCostoReferencial().signum()<0)throw new IllegalArgumentException("Costo debe ser cero o positivo");if(a.getPrecioVenta()!=null&&a.getPrecioVenta().signum()<0)throw new IllegalArgumentException("Precio de venta debe ser cero o positivo");}

    public Articulo crear(Articulo articulo) {
        validar(articulo); articulo.setId(null);
        articuloRepository.findByCodigo(articulo.getCodigo()).ifPresent(a -> {
            throw new IllegalArgumentException("Ya existe un articulo con codigo: " + a.getCodigo());
        });
        return articuloRepository.save(articulo);
    }

    @Transactional(readOnly = true)
    public Articulo obtener(Long id) {
        return articuloRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Articulo no encontrado: " + id));
    }

    /** Usado por Inventario, Produccion, Compras y Ventas para validar/mostrar datos del articulo. */
    @Transactional(readOnly = true)
    public Articulo obtenerPorCodigo(String codigo) {
        return articuloRepository.findByCodigo(codigo)
                .orElseThrow(() -> new IllegalArgumentException("Articulo no encontrado: " + codigo));
    }

    @Transactional(readOnly = true)
    public List<Articulo> listar() {
        return articuloRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Articulo> listarPorTipo(Articulo.TipoArticulo tipo) {
        return articuloRepository.findByTipoArticuloAndActivoTrue(tipo);
    }

    /**
     * Vista publica para el Portal Web: solo productos terminados activos,
     * y sin costo ni stock (ver {@link ArticuloPublicoResponse}).
     */
    @Transactional(readOnly = true)
    public List<ArticuloPublicoResponse> catalogoPublico() {
        return articuloRepository.findByTipoArticuloAndActivoTrue(Articulo.TipoArticulo.PRODUCTO_TERMINADO)
                .stream()
                .map(ArticuloPublicoResponse::desde)
                .toList();
    }

    public Articulo actualizar(Long id, Articulo cambios) {
        validar(cambios); Articulo articulo = obtener(id);
        articulo.setTipoArticulo(cambios.getTipoArticulo());articulo.setCategoria(cambios.getCategoria());
        articulo.setNombre(cambios.getNombre());
        articulo.setDescripcion(cambios.getDescripcion());
        articulo.setImagenUrl(cambios.getImagenUrl());
        articulo.setUnidadMedida(cambios.getUnidadMedida());
        articulo.setCostoReferencial(cambios.getCostoReferencial());
        articulo.setPrecioVenta(cambios.getPrecioVenta());
        return articuloRepository.save(articulo);
    }

    public Articulo desactivar(Long id) {
        Articulo articulo = obtener(id);
        articulo.setActivo(false);
        return articuloRepository.save(articulo);
    }
}
