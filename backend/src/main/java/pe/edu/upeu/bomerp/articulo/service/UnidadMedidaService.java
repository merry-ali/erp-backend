package pe.edu.upeu.bomerp.articulo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.bomerp.articulo.model.UnidadMedida;
import pe.edu.upeu.bomerp.articulo.repository.UnidadMedidaRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UnidadMedidaService {

    private final UnidadMedidaRepository unidadMedidaRepository;

    public UnidadMedida crear(UnidadMedida unidadMedida) {
        unidadMedidaRepository.findByCodigo(unidadMedida.getCodigo()).ifPresent(u -> {
            throw new IllegalArgumentException("Ya existe una unidad de medida con codigo: " + u.getCodigo());
        });
        return unidadMedidaRepository.save(unidadMedida);
    }

    @Transactional(readOnly = true)
    public UnidadMedida obtener(Long id) {
        return unidadMedidaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Unidad de medida no encontrada: " + id));
    }

    @Transactional(readOnly = true)
    public UnidadMedida obtenerPorCodigo(String codigo) {
        return unidadMedidaRepository.findByCodigo(codigo)
                .orElseThrow(() -> new IllegalArgumentException("Unidad de medida no encontrada: " + codigo));
    }

    @Transactional(readOnly = true)
    public List<UnidadMedida> listar() {
        return unidadMedidaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<UnidadMedida> listarActivas() {
        return unidadMedidaRepository.findByActivoTrue();
    }

    public UnidadMedida desactivar(Long id) {
        UnidadMedida unidadMedida = obtener(id);
        unidadMedida.setActivo(false);
        return unidadMedidaRepository.save(unidadMedida);
    }
}
