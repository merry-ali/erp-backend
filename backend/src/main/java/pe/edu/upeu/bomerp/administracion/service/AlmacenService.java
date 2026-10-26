package pe.edu.upeu.bomerp.administracion.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.bomerp.administracion.model.Almacen;
import pe.edu.upeu.bomerp.administracion.repository.AlmacenRepository;

import java.util.List;

/**
 * Los otros modulos (Inventario, Produccion) no llaman a este service
 * directamente para leer almacenes: reciben el {@code almacenId} ya
 * validado desde el flujo de negocio y solo lo usan como referencia.
 * Este service es para que Administracion gestione el catalogo de
 * almacenes (altas, bajas, consulta).
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AlmacenService {

    private final AlmacenRepository almacenRepository;
    private final SucursalService sucursalService;

    public Almacen crear(Long sucursalId, Almacen almacen) {
        almacen.setSucursal(sucursalService.obtener(sucursalId));
        return almacenRepository.save(almacen);
    }

    @Transactional(readOnly = true)
    public List<Almacen> listarPorSucursal(Long sucursalId) {
        return almacenRepository.findBySucursalId(sucursalId);
    }

    @Transactional(readOnly = true)
    public Almacen obtener(Long id) {
        return almacenRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Almacen no encontrado: " + id));
    }

    @Transactional(readOnly = true)
    public boolean existe(Long id) {
        return almacenRepository.existsById(id);
    }
}
