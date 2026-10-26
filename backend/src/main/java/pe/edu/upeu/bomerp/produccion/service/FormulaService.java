package pe.edu.upeu.bomerp.produccion.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.bomerp.produccion.model.Formula;
import pe.edu.upeu.bomerp.produccion.repository.FormulaRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FormulaService {

    private final FormulaRepository formulaRepository;
    private final pe.edu.upeu.bomerp.articulo.service.ArticuloService articulos;

    public Formula crear(Formula formula) {
        if(formula.getDetalles()==null||formula.getDetalles().isEmpty())throw new IllegalArgumentException("La fórmula necesita al menos un insumo");
        var producto=articulos.obtenerPorCodigo(formula.getCodigoProductoTerminado());if(!producto.isActivo()||producto.getTipoArticulo()!=pe.edu.upeu.bomerp.articulo.model.Articulo.TipoArticulo.PRODUCTO_TERMINADO)throw new IllegalArgumentException("Selecciona un producto terminado activo");
        var codigos=new java.util.HashSet<String>();for(var d:formula.getDetalles()){var a=articulos.obtenerPorCodigo(d.getCodigoArticulo());if(!a.isActivo()||!codigos.add(d.getCodigoArticulo())||d.getCantidadPorUnidad()==null||d.getCantidadPorUnidad().signum()<=0)throw new IllegalArgumentException("Insumo duplicado, inactivo o cantidad inválida");if(!a.getUnidadMedida().getCodigo().equals(d.getUnidadMedida()))throw new IllegalArgumentException("Unidad del insumo no coincide");d.setId(null);}
        formula.setId(null);formula.getDetalles().forEach(detalle -> detalle.setFormula(formula));
        return formulaRepository.save(formula);
    }

    @Transactional(readOnly = true)
    public Formula obtener(Long id) {
        return formulaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Formula no encontrada: " + id));
    }

    @Transactional(readOnly = true)
    public List<Formula> listar() {
        return formulaRepository.findAll();
    }

    public Formula desactivar(Long id) {
        Formula formula = obtener(id);
        formula.setActivo(false);
        return formulaRepository.save(formula);
    }
}
