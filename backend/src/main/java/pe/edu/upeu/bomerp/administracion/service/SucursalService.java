package pe.edu.upeu.bomerp.administracion.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.bomerp.administracion.model.Sucursal;
import pe.edu.upeu.bomerp.administracion.repository.SucursalRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SucursalService {

    private final SucursalRepository sucursalRepository;
    private final EmpresaService empresaService;

    public Sucursal crear(Long empresaId, Sucursal sucursal) {
        sucursal.setEmpresa(empresaService.obtener(empresaId));
        return sucursalRepository.save(sucursal);
    }

    @Transactional(readOnly = true)
    public List<Sucursal> listarPorEmpresa(Long empresaId) {
        return sucursalRepository.findByEmpresaId(empresaId);
    }

    @Transactional(readOnly = true)
    public Sucursal obtener(Long id) {
        return sucursalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sucursal no encontrada: " + id));
    }
}
