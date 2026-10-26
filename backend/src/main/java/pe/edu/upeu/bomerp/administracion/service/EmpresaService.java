package pe.edu.upeu.bomerp.administracion.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.bomerp.administracion.model.Empresa;
import pe.edu.upeu.bomerp.administracion.repository.EmpresaRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EmpresaService {

    private final EmpresaRepository empresaRepository;

    public Empresa crear(Empresa empresa) {
        if (empresaRepository.existsByRuc(empresa.getRuc())) {
            throw new IllegalArgumentException("Ya existe una empresa con RUC " + empresa.getRuc());
        }
        return empresaRepository.save(empresa);
    }

    @Transactional(readOnly = true)
    public List<Empresa> listar() {
        return empresaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Empresa obtener(Long id) {
        return empresaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada: " + id));
    }

    public Empresa actualizar(Long id, Empresa cambios) {
        Empresa empresa = obtener(id);
        empresa.setRazonSocial(cambios.getRazonSocial());
        empresa.setNombreComercial(cambios.getNombreComercial());
        empresa.setActivo(cambios.isActivo());
        return empresaRepository.save(empresa);
    }
}
