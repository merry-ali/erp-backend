package pe.edu.upeu.bomerp.administracion.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.bomerp.administracion.model.Tienda;
import pe.edu.upeu.bomerp.administracion.repository.TiendaRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TiendaService {

    private final TiendaRepository tiendaRepository;
    private final SucursalService sucursalService;

    public Tienda crear(Long sucursalId, Tienda tienda) {
        tienda.setSucursal(sucursalService.obtener(sucursalId));
        return tiendaRepository.save(tienda);
    }

    @Transactional(readOnly = true)
    public List<Tienda> listarPorSucursal(Long sucursalId) {
        return tiendaRepository.findBySucursalId(sucursalId);
    }
}
